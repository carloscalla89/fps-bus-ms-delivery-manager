package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Component
public class DeliveryManagerFacade {

    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalServiceDispatcher;
    private OrderExternalService orderExternalServiceInkatrackerLite;
    private OrderExternalService orderExternalServiceOrderTracker;
    private OrderExternalService orderExternalServiceAudit;

    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 ObjectToMapper objectToMapper,
                                 @Qualifier("deliveryDispatcher") OrderExternalService orderExternalServiceDispatcher,
                                 @Qualifier("inkatrackerlite") OrderExternalService orderExternalServiceInkatrackerLite,
                                 @Qualifier("orderTracker") OrderExternalService orderExternalServiceOrderTracker,
                                 @Qualifier("audit") OrderExternalService orderExternalServiceAudit) {
        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.orderExternalServiceDispatcher = orderExternalServiceDispatcher;
        this.orderExternalServiceInkatrackerLite = orderExternalServiceInkatrackerLite;
        this.orderExternalServiceOrderTracker = orderExternalServiceOrderTracker;
        this.orderExternalServiceAudit = orderExternalServiceAudit;
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
                .defer(() -> Mono.just(objectToMapper.convertOrderdtoToOrderEntity(orderDto)))
                .zipWith(
                        objectToMapper.convertEntityToOrderCanonical(orderDto), (a,b) ->
                        {
                            OrderWrapperResponse r =  orderTransaction.createOrderTransaction(a, orderDto);

                            //set fulfillmentID
                            b.setId(r.getFulfillmentId());

                            // set tracker ID
                            b.setTrackerId(r.getTrackerId());

                            // set status
                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                            orderStatus.setCode(r.getOrderStatusCode());
                            orderStatus.setName(r.getOrderStatusName());
                            orderStatus.setDetail(r.getOrderStatusDetail());
                            b.setOrderStatus(orderStatus);

                            // set service of delivery or pickup on store
                            b.getOrderDetail().setServiceCode(r.getServiceCode());
                            b.getOrderDetail().setServiceName(r.getServiceName());
                            b.getOrderDetail().setServiceType(r.getServiceType());
                            b.getOrderDetail().setAttempt(r.getAttemptBilling());
                            b.getOrderDetail().setAttemptTracker(r.getAttemptTracker());

                            // set local and company names;
                            b.setCompany(r.getCompanyName());
                            b.setLocal(r.getLocalName());

                            // set Receipt
                            b.getReceipt().setType(r.getReceiptName());

                            // set Payment
                            b.getPaymentMethod().setType(r.getPaymentMethodName());

                            // attempts
                            b.setAttemptTracker(r.getAttemptTracker());
                            b.setAttempt(r.getAttemptBilling());

                            b.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

                            orderExternalServiceAudit.sendOrderReactive(b).subscribe();

                            return b;
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
                    // Reattempt to send the order some inkatracker
                    resultCanonical = orderExternalServiceDispatcher
                                            .getResultfromExternalServices(ecommercePurchaseId, actionDto)
                                            .map(r -> {

                                                int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0)+1;

                                                orderTransaction.updateOrderRetryingTracker(
                                                        iOrderFulfillment.getOrderId(), attemptTracker,
                                                        r.getOrderStatus().getCode(), r.getOrderStatus().getDetail(),
                                                        Optional.ofNullable(r.getTrackerId()).orElse(null)
                                                );
                                                r.setExternalId(iOrderFulfillment.getExternalId());

                                                orderDetail.setAttempt(Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0));
                                                orderDetail.setAttemptTracker(attemptTracker);
                                                r.setOrderDetail(orderDetail);

                                                orderExternalServiceAudit.updateOrderReactive(r).subscribe();

                                                return r;
                                            });

                    break;
                case 2:
                    // Reattempt to send the order at insink
                    resultCanonical = orderExternalServiceDispatcher
                                            .getResultfromExternalServices(ecommercePurchaseId, actionDto)
                                            .map(r -> {
                                                int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0);
                                                int attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0)+1;

                                                if (!r.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())) {
                                                    attemptTracker = Optional.of(attemptTracker).orElse(0) + 1;
                                                }

                                                orderTransaction.updateOrderRetrying(
                                                        iOrderFulfillment.getOrderId(), attempt, attemptTracker,
                                                        r.getOrderStatus().getCode(), r.getOrderStatus().getDetail(),
                                                        Optional.ofNullable(r.getExternalId()).orElse(null),
                                                        Optional.ofNullable(r.getTrackerId()).orElse(null)
                                                );

                                                orderDetail.setAttempt(attempt);
                                                orderDetail.setAttemptTracker(attemptTracker);

                                                r.setOrderDetail(orderDetail);

                                                orderExternalServiceAudit.updateOrderReactive(r).subscribe();

                                                return r;
                                            });
                    break;

                case 3:
                    // Update the status when the order was released from Dispatcher

                    log.info("Starting to update the released order when the order come from dispatcher");

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
                    result.getOrderStatus().setCode(orderStatusEntity.getCode());
                    result.getOrderStatus().setName(orderStatusEntity.getType());
                    result.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    result.setExternalId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));
                    result.getOrderStatus().setDetail(actionDto.getOrderStatusDto().getDescription());

                    orderDetail.setAttempt(attempt);
                    orderDetail.setAttemptTracker(attemptTracker);

                    result.setOrderDetail(orderDetail);

                    orderExternalServiceAudit.updateOrderReactive(result).subscribe();

                    resultCanonical = Mono.just(result);


                    break;

                case 4:
                    // call the service inkatracker-lite to update the order status (CANCEL, READY_FOR_PICKUP, DELIVERED)
                    resultCanonical = orderExternalServiceInkatrackerLite
                                            .getResultfromExternalServices(ecommercePurchaseId, actionDto)
                                            .map(r -> {
                                                orderTransaction.updateStatusOrder(iOrderFulfillment.getOrderId(), r.getOrderStatus().getCode(),
                                                        r.getOrderStatus().getDetail());

                                                log.info("[START] to registar cancelled order");
                                                if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

                                                    OrderFulfillment orderFulfillment = orderTransaction.getOrderFulfillmentById(iOrderFulfillment.getOrderId());
                                                    OrderCancelledIdentity orderCancelledIdentity = new OrderCancelledIdentity();
                                                    orderCancelledIdentity.setOrderFulfillment(orderFulfillment);

                                                    CancellationCodeReason codeReason = orderTransaction.getCancellationCodeReasonByCode(actionDto.getOrderCancelCode());

                                                    OrderCancelled orderCancelled = new OrderCancelled();
                                                    orderCancelled.setOrderCancelledIdentity(orderCancelledIdentity);
                                                    orderCancelled.setCancellationCodeReason(codeReason);
                                                    orderCancelled.setObservation(actionDto.getOrderCancelObservation());

                                                    orderTransaction.insertCancelledOrder(orderCancelled);
                                                }
                                                log.info("[START] to registar cancelled order");
                                                r.setEcommerceId(ecommercePurchaseId);
                                                r.setExternalId(iOrderFulfillment.getExternalId());
                                                r.setTrackerId(iOrderFulfillment.getTrackerId());

                                                orderExternalServiceAudit.updateOrderReactive(r).subscribe();

                                                return r;
                                            });


                    break;

                default:
                    OrderCanonical resultDefault = new OrderCanonical();
                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    resultDefault.setOrderStatus(orderStatus);
                    resultDefault.setEcommerceId(ecommercePurchaseId);

                    resultCanonical = Mono.just(resultDefault);

                    break;

            }

        } else {

            OrderCanonical resultWithoutAction = new OrderCanonical();
            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
            orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
            resultWithoutAction.setOrderStatus(orderStatus);
            resultWithoutAction.setEcommerceId(ecommercePurchaseId);

            resultCanonical = Mono.just(resultWithoutAction);
        }

        return resultCanonical;

    }

    public List<CancellationCanonical> getOrderCancellationList() {
        return objectToMapper.convertEntityOrderCancellationToCanonical(orderTransaction.getListCancelReason());
    }

    public Flux<OrderCancelledCanonical> cancelOrderProcess(CancellationDto cancellationDto) {
        log.info("[START] cancelOrderProcess");
        return Flux
                .fromIterable(orderTransaction.getListOrdersToCancel(cancellationDto.getStatusType(), cancellationDto.getServiceType()))
                .parallel()
                .runOn(Schedulers.elastic())
                .map(r -> {

                    log.info("order ecommerceId:{}",r.getEcommerceId());

                    ActionDto actionDto = new ActionDto();
                    actionDto.setAction(Constant.ActionOrder.CANCEL_ORDER.name());
                    actionDto.setOrderCancelCode(cancellationDto.getCancellationCode());
                    actionDto.setOrderCancelObservation(cancellationDto.getObservation());

                    OrderCanonical orderCanonical = orderExternalServiceInkatrackerLite
                                                            .getResultfromExternalServices(r.getEcommerceId(), actionDto)
                                                            .map(s -> {
                                                                orderTransaction.updateStatusOrder(r.getOrderId(), s.getOrderStatus().getCode(),
                                                                        s.getOrderStatus().getDetail());

                                                                log.info("[START] to registar cancelled order");
                                                                if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

                                                                    OrderFulfillment orderFulfillment = orderTransaction.getOrderFulfillmentById(r.getOrderId());
                                                                    OrderCancelledIdentity orderCancelledIdentity = new OrderCancelledIdentity();
                                                                    orderCancelledIdentity.setOrderFulfillment(orderFulfillment);

                                                                    CancellationCodeReason codeReason = orderTransaction.getCancellationCodeReasonByCode(actionDto.getOrderCancelCode());

                                                                    OrderCancelled orderCancelled = new OrderCancelled();
                                                                    orderCancelled.setOrderCancelledIdentity(orderCancelledIdentity);
                                                                    orderCancelled.setCancellationCodeReason(codeReason);
                                                                    orderCancelled.setObservation(actionDto.getOrderCancelObservation());

                                                                    orderTransaction.insertCancelledOrder(orderCancelled);
                                                                }
                                                                log.info("[START] to registar cancelled order");

                                                                return s;
                                                            }).block();

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

                    orderCancelledCanonical.setStatusCode(orderCanonical.getOrderStatus().getCode());
                    orderCancelledCanonical.setStatusName(orderCanonical.getOrderStatus().getName());
                    orderCancelledCanonical.setStatusDetail(orderCanonical.getOrderStatus().getDetail());

                    orderCanonical.setEcommerceId(r.getEcommerceId());
                    orderCanonical.setExternalId(r.getExternalId());
                    orderCanonical.setTrackerId(r.getTrackerId());

                    orderExternalServiceAudit.updateOrderReactive(orderCanonical).subscribe();

                    return orderCancelledCanonical;

                }).ordered((o1,o2) -> o2.getEcommerceId().intValue() - o1.getEcommerceId().intValue());
    }

}
