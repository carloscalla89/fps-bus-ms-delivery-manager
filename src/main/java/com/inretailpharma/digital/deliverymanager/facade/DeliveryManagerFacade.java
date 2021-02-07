package com.inretailpharma.digital.deliverymanager.facade;

import java.util.List;
import java.util.Optional;

import com.inretailpharma.digital.deliverymanager.proxy.OrderFacadeProxy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.OrderWrapperResponse;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class DeliveryManagerFacade {

    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalServiceAudit;
    private CenterCompanyService centerCompanyService;
    private ApplicationParameterService applicationParameterService;
    private final ApplicationContext context;

    private OrderFacadeProxy orderFacadeProxy;

    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 ObjectToMapper objectToMapper,
                                 @Qualifier("audit") OrderExternalService orderExternalServiceAudit,
                                 ApplicationContext context,
                                 CenterCompanyService centerCompanyService,
                                 ApplicationParameterService applicationParameterService,
                                 OrderFacadeProxy orderFacadeProxy) {

        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.orderExternalServiceAudit = orderExternalServiceAudit;
        this.centerCompanyService = centerCompanyService;
        this.applicationParameterService = applicationParameterService;
        this.context = context;
        this.orderFacadeProxy = orderFacadeProxy;

    }

    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {

        log.info("[START] createOrder facade:{}", orderDto);

        return Mono
                .defer(() -> centerCompanyService.getExternalInfo(orderDto.getCompanyCode(), orderDto.getLocalCode()))
                .zipWith(Mono.just(objectToMapper.convertOrderdtoToOrderEntity(orderDto)), (storeCenter, orderFulfillment) -> {

                    OrderWrapperResponse wrapperResponse = orderTransaction.createOrderTransaction(
                            orderFulfillment,
                            orderDto,
                            storeCenter
                    );

                    OrderCanonical orderCanonicalResponse = objectToMapper
                            .setsOrderWrapperResponseToOrderCanonical(wrapperResponse, orderDto);

                    orderExternalServiceAudit
                            .sendOrderReactive(orderCanonicalResponse)
                            .subscribe();

                    return orderCanonicalResponse;
                })
                .flatMap(order -> {
                    log.info("[START] Preparation to send order:{}, companyCode:{}, status:{}", order.getEcommerceId(),
                            order.getCompanyCode(), order.getOrderStatus());

                    if (order.getOrderDetail().isServiceEnabled()
                            && Constant.OrderStatus.getByCode(order.getOrderStatus().getCode()).isSuccess()
                            && checkIfOrderIsRoutable(order)) {

                        OrderExternalService orderExternalServiceTracker = (OrderExternalService) context.getBean(
                                Constant.TrackerImplementation.getByCode(order.getOrderDetail().getServiceCode()).getName()
                        );

                        IOrderFulfillment iOrderFulfillment = orderTransaction.getOrderByecommerceId(order.getEcommerceId());
                        List<IOrderItemFulfillment> listItems = orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId());
                        StoreCenterCanonical storeCenterCanonical = objectToMapper.getStoreCenterFromOrderCanonical(order);

                        return orderExternalServiceTracker
                                .sendOrderToTracker(
                                        iOrderFulfillment,
                                        listItems,
                                        storeCenterCanonical,
                                        iOrderFulfillment.getExternalId(),
                                        order.getOrderStatus().getDetail(),
                                        Optional.ofNullable(order.getOrderStatus().getName())
                                                .filter(r -> r.equalsIgnoreCase(Constant.OrderStatusTracker.CONFIRMED.name()))
                                                .map(r -> Constant.OrderStatusTracker.CONFIRMED_TRACKER.name())
                                                .orElse(order.getOrderStatus().getName()),
                                        Optional.ofNullable(order.getOrderStatus())
                                                .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                .orElse(null),
                                        Optional.ofNullable(order.getOrderStatus())
                                                .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getReason())
                                                .orElse(null)
                                )
                                .flatMap(s -> {

                                    orderTransaction.updateOrderRetryingTracker(
                                            s.getId(), Optional.ofNullable(s.getAttemptTracker()).orElse(0) + 1,
                                            s.getOrderStatus().getCode(), s.getOrderStatus().getDetail(), s.getTrackerId());

                                    orderExternalServiceAudit
                                            .updateOrderReactive(s)
                                            .subscribe(rs -> log.info("result audit:{}", rs));

                                    return Mono.just(s);
                                });

                    }
                    log.info("[END] Preparation to send order:{}", order.getEcommerceId());

                    return Mono.just(order);
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error Creating the order:{} with companyCode:{} in Delivery-manager{}",
                            orderDto.getEcommercePurchaseId(), orderDto.getCompanyCode(), e.getMessage());

                    // Cuando la orden ha fallado al insertar al DM, se insertará con lo mínimo para registrarlo en la auditoría
                    OrderCanonical orderStatusCanonical = new OrderCanonical(
                            orderDto.getEcommercePurchaseId(), Constant.DeliveryManagerStatus.ORDER_FAILED.name(),
                            Constant.DeliveryManagerStatus.ORDER_FAILED.getStatus(), orderDto.getLocalCode(), orderDto.getCompanyCode()
                    );

                    return Mono.just(orderStatusCanonical);
                })
                .doOnSuccess(r -> log.info("[END] createOrder facade success"));

    }

    public Mono<OrderCanonical> getUpdateOrder(ActionDto actionDto, String ecommerceId) {
        log.info("[START] getUpdateOrder action:{}", actionDto);

        Long ecommercePurchaseId = Long.parseLong(ecommerceId);

        IOrderFulfillment iOrderFulfillmentLight = orderTransaction.getOrderLightByecommerceId(ecommercePurchaseId);
        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        if (action.name().equalsIgnoreCase(Constant.ActionOrder.FILL_ORDER.name())
                || (Optional.ofNullable(iOrderFulfillmentLight).isPresent()
                && !Constant.OrderStatus.getFinalStatusByCode(iOrderFulfillmentLight.getStatusCode())
        )
        ) {

            switch (action.getCode()) {
                case 1:


                    if (Constant.ActionOrder.ATTEMPT_TRACKER_CREATE.name().equalsIgnoreCase(actionDto.getAction())) {

                        return orderFacadeProxy
                                    .sendOrderToTracker(
                                            ecommercePurchaseId,
                                            iOrderFulfillmentLight.getExternalId(),
                                            iOrderFulfillmentLight.getServiceTypeCode(),
                                            null,
                                            Constant.OrderStatus.CONFIRMED_TRACKER.name(),
                                            null,
                                            null
                                            );

                    } else {
                        // actualizando la orden a tracker

                        actionDto
                                .setExternalBillingId(ecommercePurchaseId.toString());


                        return orderFacadeProxy
                                    .sendToUpdateOrder(
                                            iOrderFulfillmentLight.getOrderId(),
                                            iOrderFulfillmentLight.getEcommerceId(),
                                            iOrderFulfillmentLight.getExternalId(),
                                            actionDto,
                                            iOrderFulfillmentLight.getServiceType(),
                                            iOrderFulfillmentLight.getServiceTypeCode(),
                                            iOrderFulfillmentLight.getSource(),
                                            iOrderFulfillmentLight.getCompanyCode(),
                                            iOrderFulfillmentLight.getCenterCode(),
                                            iOrderFulfillmentLight.getStatusCode(),
                                            iOrderFulfillmentLight.getSendNewFlow()
                                    );
                    }


                case 2:

                    IOrderFulfillment iOrderFulfillmentCase2 = orderTransaction.getOrderByecommerceId(ecommercePurchaseId);

                    OrderExternalService orderExternalServiceDispatcher = (OrderExternalService) context.getBean(
                            Constant.DispatcherImplementation.getByCompanyCode(iOrderFulfillmentCase2.getCompanyCode()).getName()
                    );

                    return centerCompanyService
                            .getExternalInfo(iOrderFulfillmentCase2.getCompanyCode(), iOrderFulfillmentCase2.getCenterCode())
                            .flatMap(storeCenterCanonical -> orderExternalServiceDispatcher
                                    .sendOrderEcommerce(
                                            iOrderFulfillmentCase2,
                                            orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillmentCase2.getOrderId()),
                                            action.name(),
                                            storeCenterCanonical
                                    )
                                    .flatMap(orderResp -> {
                                        log.info("Response status from dispatcher:{}", orderResp.getOrderStatus());
                                        if ((Constant
                                                .OrderStatus
                                                .getByCode(Optional
                                                        .ofNullable(orderResp.getOrderStatus())
                                                        .map(OrderStatusCanonical::getCode)
                                                        .orElse(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                                                ).isSuccess())) {



                                            return orderFacadeProxy
                                                            .sendOrderToTracker(
                                                                    ecommercePurchaseId,
                                                                    orderResp.getExternalId(),
                                                                    iOrderFulfillmentLight.getServiceTypeCode(),

                                                                    Optional.ofNullable(orderResp.getOrderStatus())
                                                                            .filter(d -> !StringUtils.isEmpty(d.getDetail()))
                                                                            .map(OrderStatusCanonical::getDetail)
                                                                            .orElse(null),

                                                                    Optional.ofNullable(orderResp.getOrderStatus())
                                                                            .filter(r -> r.getName().equalsIgnoreCase(Constant.OrderStatusTracker.CONFIRMED.name()))
                                                                            .map(r -> Constant.OrderStatusTracker.CONFIRMED_TRACKER.name())
                                                                            .orElse(Optional.ofNullable(orderResp.getOrderStatus()).map(OrderStatusCanonical::getName).orElse(null)),

                                                                    Optional.ofNullable(orderResp.getOrderStatus())
                                                                            .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                                            .orElse(null),
                                                                    Optional.ofNullable(orderResp.getOrderStatus())
                                                                            .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getReason())
                                                                            .orElse(null)

                                                            );

                                        } else {
                                            return Mono.just(orderFacadeProxy.processTransaction(iOrderFulfillmentCase2, orderResp));

                                        }

                                    })
                            );

                case 4:

                    return orderFacadeProxy
                                    .sendToUpdateOrder(
                                            iOrderFulfillmentLight.getOrderId(),
                                            iOrderFulfillmentLight.getEcommerceId(),
                                            iOrderFulfillmentLight.getExternalId(),
                                            actionDto,
                                            iOrderFulfillmentLight.getServiceType(),
                                            iOrderFulfillmentLight.getServiceTypeCode(),
                                            iOrderFulfillmentLight.getSource(),
                                            iOrderFulfillmentLight.getCompanyCode(),
                                            iOrderFulfillmentLight.getCenterCode(),
                                            iOrderFulfillmentLight.getStatusCode(),
                                            iOrderFulfillmentLight.getSendNewFlow()
                                    );

                case 5:
                    // action to fill order from ecommerce
                    log.info("Action to fill order {} from ecommerce:", ecommercePurchaseId);

                    OrderExternalService orderExternalDispatcher = (OrderExternalService) context.getBean(
                            Constant.DispatcherImplementation.getByCompanyCode(actionDto.getCompanyCode()).getName()
                    );

                    return orderExternalDispatcher
                            .getOrderFromEcommerce(ecommercePurchaseId)
                            .flatMap(this::createOrder)
                            .defaultIfEmpty(
                                    new OrderCanonical(
                                            ecommercePurchaseId,
                                            Constant.OrderStatus.NOT_FOUND_ORDER.getCode(),
                                            Constant.OrderStatus.NOT_FOUND_ORDER.name())
                            );

                default:
                    OrderCanonical resultDefault = new OrderCanonical();
                    OrderStatusCanonical orderStatusNotFound = new OrderStatusCanonical();
                    orderStatusNotFound.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    orderStatusNotFound.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    orderStatusNotFound.setStatusDate(DateUtils.getLocalDateTimeNow());
                    resultDefault.setOrderStatus(orderStatusNotFound);

                    resultDefault.setEcommerceId(ecommercePurchaseId);

                    return Mono.just(resultDefault);
            }

        } else {

            return Optional
                    .ofNullable(iOrderFulfillmentLight)
                    .map(s -> {

                        OrderStatusCanonical os = new OrderStatusCanonical();
                        os.setCode(Constant.OrderStatus.END_STATUS_RESULT.getCode());
                        os.setName(Constant.OrderStatus.END_STATUS_RESULT.name());
                        os.setDetail("The order cant reattempted");
                        os.setStatusDate(DateUtils.getLocalDateTimeNow());

                        log.info("The order has end status:{}", os);

                        OrderCanonical resultWithoutAction = new OrderCanonical();

                        resultWithoutAction.setOrderStatus(os);
                        resultWithoutAction.setEcommerceId(ecommercePurchaseId);

                        return Mono.just(resultWithoutAction);

                    }).orElseGet(() -> {

                        OrderStatusCanonical os = new OrderStatusCanonical();
                        os.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
                        os.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
                        os.setDetail("The order not found");
                        os.setStatusDate(DateUtils.getLocalDateTimeNow());

                        log.info("The order has end status:{}", os);

                        OrderCanonical resultOrderNotFound = new OrderCanonical();


                        resultOrderNotFound.setOrderStatus(os);
                        resultOrderNotFound.setEcommerceId(ecommercePurchaseId);

                        return Mono.just(resultOrderNotFound);

                    });

        }

    }

    public Mono<OrderResponseCanonical> getOrderByOrderNumber(Long orderNumber) {
        log.info("START CALL FACADE getOrderByOrderNumber:" + orderNumber);
        return Mono.fromCallable(() -> orderTransaction.getOrderByOrderNumber(orderNumber))
                .flatMap(x -> {
                    log.info("x.isPresent()--->:" + x.isPresent());
                    if (!x.isPresent()) return Mono.empty();
                    log.info("x.get()--->:" + x.get());
                    IOrderResponseFulfillment orderResponseFulfillment = x.get();
                    log.info("OrderResponseFulfillment--->:" + orderResponseFulfillment);
                    OrderResponseCanonical orderResponseCanonical = OrderResponseCanonical.builder()
                            .creditCardId(orderResponseFulfillment.getCreditCardId())
                            .paymentMethodId(orderResponseFulfillment.getPaymentMethodId())
                            .confirmedOrder(orderResponseFulfillment.getConfirmedOrder())
                            .build();
                    log.info("END FACADE getOrderByOrderNumber:" + orderNumber);
                    return Mono.just(orderResponseCanonical);
                })
                .onErrorResume(e -> {
                    log.info("ERROR ON CALL ORDEN TRANSACTION");
                    throw new RuntimeException("Error on get order by orderNumber..." + e);
                });
    }

    public Mono<OrderCanonical> getUpdatePartialOrder(OrderDto partialOrderDto) {
        log.info("[START getUpdatePartialOrder]");
        log.info("request partialOrderDto: {}", partialOrderDto);
        try {
            return Mono.just(orderTransaction.updatePartialOrder(partialOrderDto));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR at updating the order:{}", e.getMessage());
            OrderCanonical resultDefault = new OrderCanonical();
            OrderStatusCanonical orderStatusNotFound = new OrderStatusCanonical();
            orderStatusNotFound.setCode(Constant.OrderTrackerResponseCode.ERROR_CODE);
            orderStatusNotFound.setName(Constant.OrderTrackerResponseCode.ERROR_CODE);
            orderStatusNotFound.setStatusDate(DateUtils.getLocalDateTimeNow());
            return Mono.just(resultDefault);
        }
    }

    private boolean checkIfOrderIsRoutable(OrderCanonical order) {
        String key = Constant.ApplicationsParameters.ACTIVATED_SEND_ + order.getSource() + "_" + order.getCompanyCode();
        log.info("validating if order {} is routable: key {}", order.getEcommerceId(), key);

        return Optional.ofNullable(applicationParameterService.getApplicationParameterByCodeIs(key))
                .map(param -> {
                    log.info("key {} found: value {}", param.getCode(), param.getValue());
                    return Constant.Logical.getByValueString(param.getValue()).value();
                })
                .orElseGet(() -> {
                    log.error("ERROR key {} not found");
                    return false;
                });
    }

}
