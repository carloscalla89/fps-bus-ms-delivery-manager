package com.inretailpharma.digital.deliverymanager.facade;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inretailpharma.digital.deliverymanager.adapter.*;
import com.inretailpharma.digital.deliverymanager.strategy.IActionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
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
    private IStoreAdapter iStoreAdapter;
    private ITrackerAdapter iTrackerAdapter;
    private IAuditAdapter iAuditAdapter;
    private LiquidationFacade liquidationFacade;
    private final Map<Constant.ActionOrder, IActionStrategy> actionsProcessors;

    @Autowired
    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 ObjectToMapper objectToMapper,
                                 @Qualifier("storeAdapter") IStoreAdapter iStoreAdapter,
                                 @Qualifier("trackerAdapter") ITrackerAdapter iTrackerAdapter,
                                 @Qualifier("auditAdapter") IAuditAdapter iAuditAdapter,
                                 LiquidationFacade liquidationFacade) {

        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.iStoreAdapter = iStoreAdapter;
        this.iTrackerAdapter = iTrackerAdapter;
        this.iAuditAdapter = iAuditAdapter;
        this.liquidationFacade = liquidationFacade;
        actionsProcessors = Arrays
                                .stream(Constant.ActionOrder.values())
                                .collect(Collectors.toMap(p -> p, Constant.ActionOrder::getiActionStrategy));

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


                    liquidationFacade.createUpdate(orderCanonicalResponse);

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
                                            ActionDto.builder().action(Constant.ActionOrder.FILL_ORDER.name()).build(),
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

                                    )
                                    .flatMap(response ->
                                            updateOrderInfulfillment(
                                                    response,
                                                    order.getId(),
                                                    order.getEcommerceId(),
                                                    order.getExternalId(),
                                                    null,
                                                    null,
                                                    order.getSource(),
                                                    Constant.TrackerImplementation
                                                            .getClassImplement(order.getOrderDetail().getServiceClassImplement())
                                                            .getTargetName(),
                                                    Constant.UPDATED_BY_INIT,
                                                    null
                                            )
                                    )
                                    .flatMap(response -> iAuditAdapter.updateAudit(response, Constant.UPDATED_BY_INIT))
                                    .flatMap(response -> liquidationFacade.evaluateUpdate(Constant.ActionOrder.FILL_ORDER.name(), response ));

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

        IActionStrategy actionStrategy = actionsProcessors.get(Constant.ActionOrder.getByName(actionDto.getAction()));

        Objects.requireNonNull(actionStrategy, "No processor for " + actionDto.getAction() + " found");

        return actionStrategy
                    .evaluate(actionDto, ecommerceId)
                    .flatMap(response -> liquidationFacade.evaluateUpdate(Constant.ActionOrder.FILL_ORDER.name(), response));

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