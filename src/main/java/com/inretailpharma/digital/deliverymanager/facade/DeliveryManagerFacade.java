package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.client.ProductClient;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderDetailCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.entity.OrderWrapperResponse;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.EcommerceMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;

import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;

import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Component
public class DeliveryManagerFacade {

    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalServiceDispatcher;
    private OrderExternalService orderExternalServiceAudit;
    private ApplicationParameterService applicationParameterService;
    private ProductClient productClient;
    private EcommerceMapper ecommerceMapper;

    private CenterCompanyService centerCompanyService;
    private OrderCancellationService orderCancellationService;
    private final ApplicationContext context;

    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 ObjectToMapper objectToMapper,
                                 @Qualifier("deliveryDispatcher") OrderExternalService orderExternalServiceDispatcher,
                                 @Qualifier("audit") OrderExternalService orderExternalServiceAudit,
                                 ApplicationParameterService applicationParameterService,
                                 ProductClient productClient,
                                 ApplicationContext context,
                                 EcommerceMapper ecommerceMapper,
                                 CenterCompanyService centerCompanyService,
                                 OrderCancellationService orderCancellationService) {
        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.orderExternalServiceDispatcher = orderExternalServiceDispatcher;
        this.orderExternalServiceAudit = orderExternalServiceAudit;

        this.applicationParameterService = applicationParameterService;
        this.productClient = productClient;
        this.centerCompanyService = centerCompanyService;
        this.orderCancellationService = orderCancellationService;
        this.context = context;
        this.ecommerceMapper = ecommerceMapper;
    }

    public Flux<OrderCanonical> getOrdersByStatus(String status) {

        return Flux.fromIterable(orderTransaction.getOrdersByStatus(status).stream().map(r -> {
            OrderCanonical orderCanonical = new OrderCanonical();
            orderCanonical.setEcommerceId(r.getEcommerceId());
            orderCanonical.setExternalId(r.getExternalId());
            return orderCanonical;
        }).collect(Collectors.toList()));

    }

    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {

        log.info("[START] createOrder facade");

        return Mono
                .defer(() -> centerCompanyService.getExternalInfo(orderDto.getCompanyCode(), orderDto.getLocalCode()))
                .zipWith(objectToMapper.convertOrderDtoToOrderCanonical(orderDto), (storeCenter,b) -> {

                    OrderWrapperResponse r =  orderTransaction.createOrderTransaction(
                                                    objectToMapper.convertOrderdtoToOrderEntity(orderDto),
                                                    orderDto,
                                                    storeCenter
                                              );

                    OrderCanonical orderCanonicalResponse =  objectToMapper.setsOrderWrapperResponseToOrderCanonical(r, b);

                    orderExternalServiceAudit.sendOrderReactive(orderCanonicalResponse).subscribe();

                    return orderCanonicalResponse;
                })
                .map(r -> {
                    log.info("[START] Preparation to send order some tracker with service-detail:{} and ecommerceId:{}",
                            r.getOrderDetail(), r.getEcommerceId());

                    if (r.getOrderDetail().isServiceEnabled()
                            && Constant.OrderStatus.getByCode(r.getOrderStatus().getCode()).isSendTracker()
                        && (r.getOrderStatus().getCode().equalsIgnoreCase("00")
                            || r.getOrderStatus().getCode().equalsIgnoreCase("10")
                            || r.getOrderStatus().getCode().equalsIgnoreCase("11")
                            || r.getOrderStatus().getCode().equalsIgnoreCase("37"))) {

                        OrderExternalService orderExternalService = (OrderExternalService)context.getBean(
                                Constant.TrackerImplementation.getByCode(r.getOrderDetail().getServiceCode()).getName()
                        );

                        orderExternalService.sendOrderToTracker(r)
                                            .flatMap(s -> {

                                                orderTransaction.updateOrderRetryingTracker(
                                                        r.getId(), Optional.ofNullable(s.getAttemptTracker()).orElse(0)+1,
                                                        s.getOrderStatus().getCode(), s.getOrderStatus().getDetail(), s.getTrackerId());

                                                return Mono.just(s);
                                            })
                                            .subscribe(s -> orderExternalServiceAudit.updateOrderReactive(s));

                    }
                    log.info("[END] Preparation to send order some tracker with service-detail:{} and ecommerceId:{}",
                            r.getOrderDetail(), r.getEcommerceId());
                    return r;
                })
                .doOnSuccess(r -> log.info("[END] createOrder facade"));

    }

    public Mono<OrderCanonical> getUpdateOrder(ActionDto actionDto, String ecommerceId) {
        log.info("[START] getUpdateOrder action:{}",actionDto);

        Long ecommercePurchaseId = Long.parseLong(ecommerceId);

        Mono<OrderCanonical>  resultCanonical;

        IOrderFulfillment iOrderFulfillment = orderTransaction.getOrderByecommerceId(ecommercePurchaseId);
        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        if (Optional.ofNullable(iOrderFulfillment).isPresent()) {

            OrderDetailCanonical orderDetail = new OrderDetailCanonical();

            switch (action.getCode()) {
                case 1:

                    OrderExternalService orderExternalService = (OrderExternalService)context.getBean(
                            Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getName()
                    );

                    Optional.ofNullable(iOrderFulfillment.getExternalId())
                            .ifPresent(r -> actionDto.setExternalBillingId(r.toString()));


                    if (Constant.ActionOrder.ATTEMPT_TRACKER_CREATE.name().equalsIgnoreCase(actionDto.getAction())) {

                        resultCanonical = centerCompanyService.getExternalInfo(iOrderFulfillment.getCompanyCode(), iOrderFulfillment.getCenterCode())
                                                              .flatMap(r -> {
                                                                 OrderCanonical orderCanonical =  objectToMapper.convertIOrderDtoToOrderFulfillmentCanonical(iOrderFulfillment);
                                                                 orderCanonical.setLocalId(r.getLegacyId());
                                                                 orderCanonical.setLocalCode(r.getLocalCode());
                                                                 orderCanonical.setLocalDescription(r.getDescription());
                                                                 orderCanonical.setLocalLatitude(r.getLatitude());
                                                                 orderCanonical.setLocalLongitude(r.getLongitude());

                                                                 return Mono.just(orderCanonical);

                                                              }).flatMap(r -> orderExternalService.sendOrderToTracker(r));


                    } else {
                        resultCanonical = orderExternalService
                                                .getResultfromExternalServices(ecommercePurchaseId, actionDto, iOrderFulfillment.getCompanyCode())
                                                .map(r -> processInkatrackers(iOrderFulfillment, r));
                    }

                    break;
                case 2:

                    // Reattempt to send the order at insink
                    resultCanonical = orderExternalServiceDispatcher
                                            .getResultfromExternalServices(ecommercePurchaseId, actionDto, iOrderFulfillment.getCompanyCode())
                                            .flatMap(r -> {

                                                r.setAttempt(Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0)+1);

                                                if (r.getOrderStatus().getCode().equalsIgnoreCase("00")
                                                        || r.getOrderStatus().getCode().equalsIgnoreCase("10")) {

                                                    r.setAttemptTracker(Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0));

                                                    OrderExternalService service = (OrderExternalService)context.getBean(
                                                            Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getName()
                                                    );

                                                    return service.sendOrderToTracker(r).flatMap(s -> {

                                                        orderTransaction.updateOrderRetrying(
                                                                iOrderFulfillment.getOrderId(), s.getAttempt(), s.getAttemptTracker(),
                                                                s.getOrderStatus().getCode(), s.getOrderStatus().getDetail(),
                                                                Optional.ofNullable(s.getExternalId()).orElse(null),
                                                                Optional.ofNullable(s.getExternalId()).orElse(null)
                                                        );

                                                        orderExternalServiceAudit.updateOrderReactive(s).subscribe();

                                                        return Mono.just(s);
                                                    });

                                                } else {
                                                    if (r.getOrderStatus() != null && r.getOrderStatus().getCode() != null &&
                                                            r.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER.getCode()) &&
                                                            Optional.ofNullable(iOrderFulfillment.getPaymentType()).orElse(PaymentMethod.PaymentType.CASH.name())
                                                                    .equalsIgnoreCase(PaymentMethod.PaymentType.ONLINE_PAYMENT.name())) {

                                                        r.getOrderStatus().setCode(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.getCode());
                                                        r.getOrderStatus().setName(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.name());
                                                    }

                                                    orderExternalServiceAudit.updateOrderReactive(r).subscribe();

                                                    return Mono.just(r);
                                                }

                                            });
                    break;

                case 3:
                    // Update the status when the order was released from Dispatcher
                    log.info("Starting to update the released order when the order come from dispatcher:{}",ecommerceId);

                    int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0);
                    int attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0) +1;

                    OrderDto orderDto = new OrderDto();
                    orderDto.setExternalPurchaseId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));
                    orderDto.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    orderDto.setOrderStatusDto(actionDto.getOrderStatusDto());

                    OrderStatus orderStatusEntity =  orderTransaction.getStatusOrderFromDeliveryDispatcher(orderDto);

                    if (!orderStatusEntity.getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode())) {
                        attemptTracker = attemptTracker + 1;
                    }

                    orderTransaction.updateReservedOrder(
                            iOrderFulfillment.getOrderId(),
                            Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null),
                            attempt,
                            orderStatusEntity.getCode(),
                            actionDto.getOrderStatusDto().getDescription()
                    );

                    OrderCanonical result = new OrderCanonical();

                    result.setEcommerceId(iOrderFulfillment.getEcommerceId());

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(orderStatusEntity.getCode());
                    orderStatus.setName(orderStatusEntity.getType());
                    orderStatus.setDetail(actionDto.getOrderStatusDto().getDescription());
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                    result.setOrderStatus(orderStatus);

                    result.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    result.setExternalId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));


                    orderDetail.setAttempt(attempt);
                    orderDetail.setAttemptTracker(attemptTracker);

                    result.setOrderDetail(orderDetail);

                    result.setBridgePurchaseId(iOrderFulfillment.getBridgePurchaseId());

                    orderExternalServiceAudit.updateOrderReactive(result).subscribe();

                    resultCanonical = Mono.just(result);


                    break;

                case 4:
                    // call the service inkatracker-lite or inkatracker to update the order status (CANCEL, READY_FOR_PICKUP, DELIVERED)
                    log.info("Service Type Code:{}",iOrderFulfillment.getServiceTypeCode());

                    OrderExternalService orderExternalService2 = (OrderExternalService)context.getBean(
                            Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getName()
                    );

                    if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {
                        CancellationCodeReason codeReason;
                        if (actionDto.getOrderCancelCode() != null && actionDto.getOrderCancelAppType() != null) {
                            codeReason = orderCancellationService
                                            .geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType());
                        } else {
                            codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
                        }

                        Optional.ofNullable(codeReason)
                                .ifPresent(r -> {
                                    actionDto.setOrderCancelAppType(r.getAppType());
                                    actionDto.setOrderCancelReason(r.getReason());
                                    actionDto.setOrderCancelClientReason(r.getClientReason());
                                });
                    }

                    actionDto.setExternalBillingId(Optional.ofNullable(iOrderFulfillment.getExternalId()).map(Object::toString).orElse("0"));

                    resultCanonical = orderExternalService2
                                            .getResultfromExternalServices(ecommercePurchaseId, actionDto, iOrderFulfillment.getCompanyCode())
                                            .map(r -> {

                                                log.info("[START] to update order");

                                                if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {
                                                    log.info("Action to cancel order");
                                                    orderTransaction.updateStatusCancelledOrder(
                                                            r.getOrderStatus().getDetail(), actionDto.getOrderCancelObservation(),
                                                            actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType(),
                                                            r.getOrderStatus().getCode(), iOrderFulfillment.getOrderId()
                                                    );
                                                } else {
                                                    orderTransaction.updateStatusOrder(iOrderFulfillment.getOrderId(), r.getOrderStatus().getCode(),
                                                            r.getOrderStatus().getDetail());
                                                }

                                                log.info("[END] to update order");

                                                r.setEcommerceId(ecommercePurchaseId);
                                                r.setExternalId(iOrderFulfillment.getExternalId());
                                                r.setTrackerId(iOrderFulfillment.getTrackerId());
                                                r.setBridgePurchaseId(iOrderFulfillment.getBridgePurchaseId());
                                                r.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

                                                orderExternalServiceAudit.updateOrderReactive(r).subscribe();

                                                return r;
                                            });


                    break;

                default:
                    OrderCanonical resultDefault = new OrderCanonical();
                    OrderStatusCanonical orderStatusNotFound = new OrderStatusCanonical();
                    orderStatusNotFound.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    orderStatusNotFound.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    orderStatusNotFound.setStatusDate(DateUtils.getLocalDateTimeNow());
                    resultDefault.setOrderStatus(orderStatusNotFound);

                    resultDefault.setEcommerceId(ecommercePurchaseId);

                    resultCanonical = Mono.just(resultDefault);

                    break;
            }

        } else {

            OrderCanonical resultWithoutAction = new OrderCanonical();
            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
            orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
            orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
            resultWithoutAction.setOrderStatus(orderStatus);
            resultWithoutAction.setEcommerceId(ecommercePurchaseId);

            resultCanonical = Mono.just(resultWithoutAction);
        }

        return resultCanonical;

    }

    private OrderCanonical applyRetryResult(IOrderFulfillment iOrderFulfillment, OrderDetailCanonical orderDetail, OrderCanonical response) {
        int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0);
        int attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0)+1;

        if (!response.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())) {
            attemptTracker = Optional.of(attemptTracker).orElse(0) + 1;
        }
/*
        // Para validar si el reintento siendo un pago en línea y una orden cancelada se ponga status 37, sino
        // tal orden si es cancelada por stock, ya no se mostraría como pendiente
        if (response.getOrderStatus() != null && response.getOrderStatus().getCode() != null &&
                response.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER.getCode()) &&
                Optional.ofNullable(iOrderFulfillment.getPaymentType()).orElse(PaymentMethod.PaymentType.CASH.name())
                        .equalsIgnoreCase(PaymentMethod.PaymentType.ONLINE_PAYMENT.name())) {

            response.getOrderStatus().setCode(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.getCode());
            response.getOrderStatus().setName(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.name());
        }
*/
        orderTransaction.updateOrderRetrying(
                iOrderFulfillment.getOrderId(), attempt, attemptTracker,
                response.getOrderStatus().getCode(), response.getOrderStatus().getDetail(),
                Optional.ofNullable(response.getExternalId()).orElse(null),
                Optional.ofNullable(response.getTrackerId()).orElse(null)
        );

        orderDetail.setAttempt(attempt);
        orderDetail.setAttemptTracker(attemptTracker);

        response.setOrderDetail(orderDetail);

        response.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

        response.setBridgePurchaseId(iOrderFulfillment.getBridgePurchaseId());

        orderExternalServiceAudit.updateOrderReactive(response).subscribe();

        return response;
    }

    public List<CancellationCanonical> getOrderCancellationList() {
        return objectToMapper.convertEntityOrderCancellationToCanonical(orderTransaction.getListCancelReason());
    }

    public Flux<OrderCancelledCanonical> cancelOrderProcess(CancellationDto cancellationDto) {
        log.info("[START] cancelOrderProcess");

        ApplicationParameter daysValue = applicationParameterService
                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET);

        return Flux
                .fromIterable(orderTransaction
                                .getListOrdersToCancel(
                                        cancellationDto.getServiceType(), cancellationDto.getCompanyCode(),
                                        Integer.parseInt(daysValue.getValue()), cancellationDto.getStatusType()
                                )
                )
                .parallel()
                    .runOn(Schedulers.elastic())
                .map(r -> {

                    log.info("order info- companyCode:{}, centerCode:{}, ecommerceId:{}, ",
                            r.getCompanyCode(), r.getCenterCode(), r.getEcommerceId());

                    ActionDto actionDto = new ActionDto();
                    actionDto.setAction(Constant.ActionOrder.CANCEL_ORDER.name());
                    actionDto.setOrderCancelCode(cancellationDto.getCancellationCode());
                    actionDto.setOrderCancelObservation(cancellationDto.getObservation());

                    OrderCanonical orderCanonical = orderExternalServiceInkatrackerLite
                                                            .getResultfromExternalServices(r.getEcommerceId(), actionDto, "")
                                                            .map(s -> {
                                                                log.info("[START] Processing the updating of cancelled order");

                                                                orderTransaction.updateStatusCancelledOrder(
                                                                        s.getOrderStatus().getDetail(), actionDto.getOrderCancelObservation(),
                                                                        actionDto.getOrderCancelCode(),
                                                                        s.getOrderStatus().getCode(), r.getOrderId()
                                                                );
                                                                log.info("[END] Processing the updating of cancelled order");
                                                                return s;
                                                            }).defaultIfEmpty(
                                                                    new OrderCanonical(
                                                                            r.getEcommerceId(),
                                                                            Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.getCode(),
                                                                            Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.name()
                                                                    )
                                                            ).block();

                    OrderCancelledCanonical orderCancelledCanonical = new OrderCancelledCanonical();

                    orderCancelledCanonical.setEcommerceId(r.getEcommerceId());
                    orderCancelledCanonical.setExternalId(r.getExternalId());
                    orderCancelledCanonical.setCompany(r.getCompanyName());
                    orderCancelledCanonical.setLocalCode(r.getCenterCode());
                    orderCancelledCanonical.setLocal(r.getCenterName());
                    orderCancelledCanonical.setConfirmedSchedule(DateUtils.getLocalDateTimeWithFormat(r.getConfirmedSchedule()));
                    orderCancelledCanonical.setServiceCode(r.getServiceTypeCode());
                    orderCancelledCanonical.setServiceName(r.getServiceTypeName());
                    orderCancelledCanonical.setServiceType(r.getServiceType());

                    Optional.ofNullable(orderCanonical.getOrderStatus()).ifPresent(f -> {
                        orderCancelledCanonical.setStatusCode(f.getCode());
                        orderCancelledCanonical.setStatusName(f.getName());
                        orderCancelledCanonical.setStatusDetail(f.getDetail());
                        f.setStatusDate(DateUtils.getLocalDateTimeNow());
                    });

                    orderCanonical.setEcommerceId(r.getEcommerceId());
                    orderCanonical.setExternalId(r.getExternalId());
                    orderCanonical.setTrackerId(r.getTrackerId());

                    orderExternalServiceAudit.updateOrderReactive(orderCanonical).subscribe();

                    return orderCancelledCanonical;

                }).ordered((o1,o2) -> o2.getEcommerceId().intValue() - o1.getEcommerceId().intValue());
    }

    private OrderCanonical processInkatrackers(IOrderFulfillment iOrderFulfillment, OrderCanonical r) {

        OrderDetailCanonical orderDetail = new OrderDetailCanonical();

        int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0)+1;

        orderTransaction.updateOrderRetryingTracker(
                iOrderFulfillment.getOrderId(), attemptTracker,
                r.getOrderStatus().getCode(), r.getOrderStatus().getDetail(),
                Optional.ofNullable(r.getTrackerId()).orElse(null)
        );
        r.setExternalId(iOrderFulfillment.getExternalId());
        r.setBridgePurchaseId(iOrderFulfillment.getBridgePurchaseId());

        orderDetail.setAttempt(Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0));
        orderDetail.setAttemptTracker(attemptTracker);

        r.setOrderDetail(orderDetail);

        r.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

        orderExternalServiceAudit.updateOrderReactive(r).subscribe();

        return r;
    }

}
