package com.inretailpharma.digital.deliverymanager.facade;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inretailpharma.digital.deliverymanager.adapter.*;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DeliveryManagerFacade extends FacadeAbstractUtil {

    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private OrderFacadeProxy orderFacadeProxy;
    private OrderCancellationService orderCancellationService;

    private IStoreAdapter iStoreAdapter;
    private ITrackerAdapter iTrackerAdapter;
    private IDeliveryDispatcherAdapter iDeliveryDispatcherAdapter;
    private IAuditAdapter iAuditAdapter;

    @Autowired
    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 ObjectToMapper objectToMapper,
                                 OrderFacadeProxy orderFacadeProxy, OrderCancellationService orderCancellationService,
                                 @Qualifier("storeAdapter") IStoreAdapter iStoreAdapter,
                                 @Qualifier("trackerAdapter") ITrackerAdapter iTrackerAdapter,
                                 @Qualifier("dispatcher") IDeliveryDispatcherAdapter iDeliveryDispatcherAdapter,
                                 @Qualifier("auditAdapter") IAuditAdapter iAuditAdapter) {

        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.orderFacadeProxy = orderFacadeProxy;
        this.orderCancellationService = orderCancellationService;
        this.iStoreAdapter = iStoreAdapter;
        this.iTrackerAdapter = iTrackerAdapter;
        this.iDeliveryDispatcherAdapter = iDeliveryDispatcherAdapter;
        this.iAuditAdapter = iAuditAdapter;

    }

    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {

        try {
            log.info("[START] create-order:{}", new ObjectMapper().writeValueAsString(orderDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return Mono
                .defer(() -> iStoreAdapter.getStoreByCompanyCodeAndLocalCode(orderDto.getCompanyCode(), orderDto.getLocalCode()))
                .zipWith(Mono.just(objectToMapper.convertOrderdtoToOrderEntity(orderDto)), (storeCenter, orderFulfillment) -> {

                    OrderCanonical orderCanonicalResponse = orderTransaction
                                                                .processOrderTransaction(
                                                                        orderFulfillment,
                                                                        orderDto,
                                                                        storeCenter
                                                                );
                    orderCanonicalResponse.setStoreCenter(storeCenter);


                    iAuditAdapter.createAudit(orderCanonicalResponse, Constant.UPDATED_BY_INIT);

                    return orderCanonicalResponse;
                })
                .flatMap(order -> {
                    log.info("[START] Preparation to send order:{}, companyCode:{}, status:{}, classImplement:{}",
                            order.getEcommerceId(), order.getCompanyCode(), order.getOrderStatus(),
                            order.getOrderDetail().getServiceClassImplement());

                    if (order.getOrderDetail().isServiceEnabled()
                            && Constant.OrderStatus.getByName(order.getOrderStatus().getName()).isSuccess()) {

                        return iTrackerAdapter
                                    .evaluateTracker(
                                            Constant.TrackerImplementation
                                                    .getClassImplement(order.getOrderDetail().getServiceClassImplement())
                                                    .getTrackerImplement(),
                                            new ActionDto(),
                                            order.getStoreCenter(),
                                            order.getCompanyCode(),
                                            order.getOrderDetail().getServiceType(),
                                            order.getEcommerceId(),
                                            order.getExternalId(),
                                            Optional.ofNullable(order.getOrderStatus().getName())
                                                    .filter(r -> r.equalsIgnoreCase(Constant.OrderStatusTracker.CONFIRMED.name()))
                                                    .map(r -> Constant.OrderStatusTracker.CONFIRMED_TRACKER.name())
                                                    .orElse(order.getOrderStatus().getName()),
                                            Optional.ofNullable(order.getOrderStatus())
                                                    .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                    .orElse(null),
                                            Optional.ofNullable(order.getOrderStatus())
                                                    .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getReason())
                                                    .orElse(null),
                                            null,
                                            order.getOrderStatus().getDetail()

                                    ).flatMap(response -> iAuditAdapter.updateAudit(response, Constant.UPDATED_BY_INIT));

                    }
                    log.info("[END] Preparation to send order:{}", order.getEcommerceId());

                    return Mono.just(order);
                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("Error empty Creating the order:{} with companyCode:{}",
                            orderDto.getEcommercePurchaseId(), orderDto.getCompanyCode());

                    // Cuando la orden ha fallado al insertar al DM, se insertará con lo mínimo para registrarlo en la auditoría
                    OrderCanonical orderStatusCanonical = new OrderCanonical(
                            orderDto.getEcommercePurchaseId(), Constant.DeliveryManagerStatus.ORDER_FAILED.name(),
                            Constant.DeliveryManagerStatus.ORDER_FAILED.getStatus(), orderDto.getLocalCode(), orderDto.getCompanyCode()
                    );

                    iAuditAdapter.createAudit(orderStatusCanonical, Constant.UPDATED_BY_INIT);

                    return Mono.just(orderStatusCanonical);
                }))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error Creating the order:{} with companyCode:{} in Delivery-manager{}",
                            orderDto.getEcommercePurchaseId(), orderDto.getCompanyCode(), e.getMessage());

                    // Cuando la orden ha fallado al insertar al DM, se insertará con lo mínimo para registrarlo en la auditoría
                    OrderCanonical orderStatusCanonical = new OrderCanonical(
                            orderDto.getEcommercePurchaseId(), Constant.DeliveryManagerStatus.ORDER_FAILED.name(),
                            Constant.DeliveryManagerStatus.ORDER_FAILED.getStatus(), orderDto.getLocalCode(), orderDto.getCompanyCode()
                    );

                    iAuditAdapter.createAudit(orderStatusCanonical, Constant.UPDATED_BY_INIT);

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

                    return  iStoreAdapter.getStoreByCompanyCodeAndLocalCode(
                                iOrderFulfillmentLight.getCompanyCode(), iOrderFulfillmentLight.getCenterCode()
                            ).flatMap(store -> iTrackerAdapter
                                                    .evaluateTracker(
                                                            Constant.TrackerImplementation
                                                                    .getClassImplement(iOrderFulfillmentLight.getClassImplement())
                                                                    .getTrackerImplement(),
                                                            actionDto,
                                                            store,
                                                            iOrderFulfillmentLight.getCompanyCode(),
                                                            iOrderFulfillmentLight.getServiceType(),
                                                            iOrderFulfillmentLight.getEcommerceId(),
                                                            iOrderFulfillmentLight.getExternalId(),
                                                            Constant.OrderStatus.CONFIRMED_TRACKER.name(),
                                                            null,
                                                            null,
                                                            null,
                                                            null
                                                    )
                            );

                case 2:

                    return  iStoreAdapter.getStoreByCompanyCodeAndLocalCode(
                                iOrderFulfillmentLight.getCompanyCode(), iOrderFulfillmentLight.getCenterCode()
                            )
                            .flatMap(store -> iDeliveryDispatcherAdapter
                                                    .sendRetryInsink(
                                                            iOrderFulfillmentLight.getEcommerceId(),
                                                            iOrderFulfillmentLight.getCompanyCode(),
                                                            store
                                                    )
                                                    .flatMap(orderResp -> {
                                                        log.info("Response status:{}, ecommerceId:{}, externalId:{} from dispatcher",
                                                                orderResp.getOrderStatus(), orderResp.getEcommerceId(), orderResp.getExternalId());
                                                        if ((Constant
                                                                .OrderStatus
                                                                .getByCode(Optional
                                                                        .ofNullable(orderResp.getOrderStatus())
                                                                        .map(OrderStatusCanonical::getCode)
                                                                        .orElse(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                                                                ).isSuccess())) {

                                                            iTrackerAdapter
                                                                    .evaluateTracker(
                                                                            Constant.TrackerImplementation
                                                                                    .getClassImplement(iOrderFulfillmentLight.getClassImplement())
                                                                                    .getTrackerImplement(),
                                                                            actionDto,
                                                                            store,
                                                                            iOrderFulfillmentLight.getCompanyCode(),
                                                                            iOrderFulfillmentLight.getServiceType(),
                                                                            iOrderFulfillmentLight.getEcommerceId(),
                                                                            orderResp.getExternalId(),

                                                                            Optional.ofNullable(orderResp.getOrderStatus())
                                                                                    .filter(r -> r.getName().equalsIgnoreCase(Constant.OrderStatusTracker.CONFIRMED.name()))
                                                                                    .map(r -> Constant.OrderStatusTracker.CONFIRMED_TRACKER.name())
                                                                                    .orElse(Optional.ofNullable(orderResp.getOrderStatus()).map(OrderStatusCanonical::getName).orElse(null)),

                                                                            Optional.ofNullable(orderResp.getOrderStatus())
                                                                                    .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                                                    .orElse(null),

                                                                            Optional.ofNullable(orderResp.getOrderStatus())
                                                                                    .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getReason())
                                                                                    .orElse(null),
                                                                            null,
                                                                            Optional.ofNullable(orderResp.getOrderStatus())
                                                                                    .filter(d -> !StringUtils.isEmpty(d.getDetail()))
                                                                                    .map(OrderStatusCanonical::getDetail)
                                                                                    .orElse(null)
                                                                    )
                                                                    .flatMap(response ->
                                                                            updateOrderInfulfillment(
                                                                                    response,
                                                                                    iOrderFulfillmentLight.getOrderId(),
                                                                                    iOrderFulfillmentLight.getEcommerceId(),
                                                                                    orderResp.getExternalId(),
                                                                                    Optional.ofNullable(orderResp.getOrderStatus())
                                                                                            .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                                                            .orElse(null),
                                                                                    null,
                                                                                    iOrderFulfillmentLight.getSource(),
                                                                                    Constant.TARGET_INSINK,
                                                                                    actionDto.getUpdatedBy(),
                                                                                    null

                                                                            )
                                                                    );

                                                        }

                                                        return updateOrderInfulfillment(
                                                                orderResp,
                                                                iOrderFulfillmentLight.getOrderId(),
                                                                iOrderFulfillmentLight.getEcommerceId(),
                                                                orderResp.getExternalId(),
                                                                null,
                                                                null,
                                                                iOrderFulfillmentLight.getSource(),
                                                                Constant.TARGET_INSINK,
                                                                actionDto.getUpdatedBy(),
                                                                null
                                                                );


                                                    }).flatMap(response ->
                                                        iAuditAdapter.updateAudit(response, actionDto.getUpdatedBy())
                                                    )



                            );

                case 4:

                    return orderFacadeProxy.sendToUpdateOrder(iOrderFulfillmentLight, actionDto,
                            orderCancellationService.evaluateGetCancel(actionDto));

                case 5:
                    // action to fill order from ecommerce
                    log.info("Action to fill order {} from ecommerce:", ecommercePurchaseId);

                    return iDeliveryDispatcherAdapter
                                .getOrderEcommerce(ecommercePurchaseId, iOrderFulfillmentLight.getCompanyCode())
                                .flatMap(this::createOrder)
                                .defaultIfEmpty(
                                        new OrderCanonical(
                                                ecommercePurchaseId,
                                                Constant.OrderStatus.NOT_FOUND_ORDER.getCode(),
                                                Constant.OrderStatus.NOT_FOUND_ORDER.name())
                                );
                case 6:

                    return orderFacadeProxy
                            .getfromOnlinePaymentExternalServices(
                                    iOrderFulfillmentLight.getOrderId(),
                                    iOrderFulfillmentLight.getEcommerceId(),
                                    iOrderFulfillmentLight.getSource(),
                                    iOrderFulfillmentLight.getServiceTypeShortCode(),
                                    iOrderFulfillmentLight.getCompanyCode(),
                                    actionDto);

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
        	
        	if((actionDto.getOrderCancelCode() != null && 
        			actionDto.getOrderCancelCode().equalsIgnoreCase(Constant.ORIGIN_BBR)) || 
        			(actionDto.getOrderCancelObservation() != null && actionDto.getOrderCancelObservation().indexOf(Constant.ORIGIN_BBR) != -1)) {
        		
        		return orderFacadeProxy
                        .getfromOnlinePaymentExternalServices(
                                iOrderFulfillmentLight.getOrderId(),
                                iOrderFulfillmentLight.getEcommerceId(),
                                iOrderFulfillmentLight.getSource(),
                                iOrderFulfillmentLight.getServiceTypeShortCode(),
                                iOrderFulfillmentLight.getCompanyCode(),
                                actionDto);
            	
            }

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
                            .currency(orderResponseFulfillment.getCurrency())
                            .orderStatus(orderResponseFulfillment.getStatusName())
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

}