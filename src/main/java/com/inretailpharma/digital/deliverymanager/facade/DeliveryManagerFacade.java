package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalResponse;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrdersSelectedResponse;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.repository.custom.CustomQueryOrderInfo;
import com.inretailpharma.digital.deliverymanager.service.OrderInfoService;
import com.inretailpharma.digital.deliverymanager.strategy.IActionStrategy;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class DeliveryManagerFacade extends FacadeAbstractUtil {

    private OrderTransaction orderTransaction;
    private IAuditAdapter iAuditAdapter;
    private LiquidationFacade liquidationFacade;
    private Map<Constant.ActionOrder, IActionStrategy> actionsProcessors;
    private ApplicationContext context;
    private OrderExternalService orderExternalService;
    private ObjectToMapper objectMapper;
    private OrderInfoService orderInfoService;

    @Autowired
    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 @Qualifier("auditAdapter") IAuditAdapter iAuditAdapter,
                                 LiquidationFacade liquidationFacade,
                                 CustomQueryOrderInfo orderQueryFilter,
                                 OrderInfoService orderInfoService,
                                 ApplicationContext context,
                                 @Qualifier("orderTracker") OrderExternalService orderExternalService,
                                 ObjectToMapper objectMapper) {
        this.orderTransaction = orderTransaction;
        this.iAuditAdapter = iAuditAdapter;
        this.liquidationFacade = liquidationFacade;
        this.context = context;
        this.orderExternalService = orderExternalService;
        this.orderInfoService = orderInfoService;
        this.objectMapper = objectMapper;

        actionsProcessors = Arrays
                .stream(Constant.ActionOrder.values())
                .collect(Collectors.toMap(p -> p, p ->
                        (IActionStrategy)this.context.getBean(p.getActionStrategyImplement())));
    }

    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {
        return createOrderFulfillment(orderDto);
    }

    public Mono<OrderCanonical> getUpdateOrder(ActionDto actionDto, String ecommerceId, boolean notifyExternalLiquidation) {
        log.info("[START] getUpdateOrder action:{}", actionDto);
        IActionStrategy actionStrategy = actionsProcessors.get(Constant.ActionOrder.getByName(actionDto.getAction()));
        if (actionStrategy == null) {
            OrderStatusCanonical os = new OrderStatusCanonical();
            os.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
            os.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
            os.setDetail("The action " + actionDto.getAction() + " not exist");
            os.setStatusDate(DateUtils.getLocalDateTimeNow());
            OrderCanonical resultOrderNotFound = new OrderCanonical();
            resultOrderNotFound.setOrderStatus(os);
            resultOrderNotFound.setEcommerceId(Long.parseLong(ecommerceId));
            return Mono.just(resultOrderNotFound);
        }
        return Mono.fromCallable(() ->
                        actionStrategy.evaluate(actionDto, ecommerceId)
                                .flatMap(response ->
                                        liquidationFacade.evaluateUpdate(response, actionDto, notifyExternalLiquidation))
                                .subscribeOn(Schedulers.boundedElastic()))
                .flatMap(val -> val);
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

    public Mono<OrderCanonicalResponse> getOrder(RequestFilterDTO filter) {
        return orderTransaction.getOrder(filter);
    }

    public Mono<OrderCanonical> getUpdatePartialOrder(OrderDto partialOrderDto) {
        log.info("[START] getUpdatePartialOrder:{}", partialOrderDto);
        return Mono
                .just(orderTransaction.updatePartialOrder(partialOrderDto))
                .flatMap(order -> {
                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.PARTIAL_UPDATE_ORDER.getCode());
                    orderStatus.setName(Constant.OrderStatus.PARTIAL_UPDATE_ORDER.getCode());
                    order.setOrderStatus(orderStatus);
                    orderExternalService.updatePartial(partialOrderDto).subscribe();
                    return Mono.just(order);
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error during update partial order:{}", e.getMessage());
                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setEcommerceId(partialOrderDto.getEcommercePurchaseId());
                    OrderStatusCanonical statusCanonical = new OrderStatusCanonical();
                    statusCanonical.setCode(Constant.OrderStatus.ERROR_PARTIAL_UPDATE.getCode());
                    statusCanonical.setName(Constant.OrderStatus.ERROR_PARTIAL_UPDATE.name());
                    statusCanonical.setDetail(e.getMessage());
                    return Mono.just(orderCanonical);
                })
                .flatMap(order -> iAuditAdapter.updateAudit(order, Constant.UPDATED_BY_INKATRACKER_WEB))
                .flatMap(order -> liquidationFacade.evaluateUpdate(
                        order,
                        ActionDto.builder()
                                .origin(Constant.ORIGIN_INKATRACKER_WEB)
                                .action(Constant.ActionOrder.SET_PARTIAL_ORDER.name()).build(),
                        true)
                );
    }

    public Mono<OrderCanonical> getUpdateOrderStorePickup(OrderDto orderDto) {

        if (!validationIfExistOrderPickup(orderDto.getEcommercePurchaseId())) {

            OrderStatusCanonical os = new OrderStatusCanonical();
            os.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            os.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
            os.setDetail("The order " + orderDto.getEcommercePurchaseId() + " not exist or this has a final status");
            os.setStatusDate(DateUtils.getLocalDateTimeNow());

            OrderCanonical resultOrderNotFound = new OrderCanonical();

            resultOrderNotFound.setOrderStatus(os);
            resultOrderNotFound.setEcommerceId((orderDto.getEcommercePurchaseId()));

            return Mono.just(resultOrderNotFound);

        }

        log.info("update order fulfillment");

        return Mono
                .just(orderTransaction.updateOrderPickup(orderDto))
                .flatMap(order -> {
                    OrderStatusCanonical updateStatus = new OrderStatusCanonical();
                    updateStatus.setCode(Constant.OrderStatus.UPDATE_PICKER_SUCCESS.getCode());
                    updateStatus.setName(Constant.OrderStatus.UPDATE_PICKER_SUCCESS.name());
                    updateStatus.setSuccessful(Constant.OrderStatus.UPDATE_PICKER_SUCCESS.isSuccess());
                    order.setOrderStatus(updateStatus);
                    return Mono.just(order);
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error during update partial order:{}", e.getMessage());
                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setEcommerceId(orderDto.getEcommercePurchaseId());
                    OrderStatusCanonical statusCanonical = new OrderStatusCanonical();
                    statusCanonical.setCode(Constant.OrderStatus.UPDATE_PICKER_ERROR.getCode());
                    statusCanonical.setName(Constant.OrderStatus.UPDATE_PICKER_ERROR.name());
                    statusCanonical.setSuccessful(Constant.OrderStatus.UPDATE_PICKER_ERROR.isSuccess());
                    statusCanonical.setDetail(e.getMessage());
                    return Mono.just(orderCanonical);
                });


    }

    public IOrderFulfillment getOrderByEcommerceID(Long ecommercePurchaseId) {
        return orderTransaction.getOrderByecommerceId(ecommercePurchaseId);
    }

    @Override
    public Mono<OrderInfoConsolidated> getOrderInfoDetail(long ecommerceId) {
        return orderInfoService.findOrderInfoClientByEcommerceId(ecommerceId);
    }

    @Override
    public OrdersSelectedResponse getOrderDetail(FilterOrderDTO filter) {
        return orderInfoService.getOrderHeaderDetails(filter);
    }

    public boolean validationIfExistOrderPickup(Long ecommerceId) {
        return  Optional
                .ofNullable(getOnlyOrderByecommerceId(ecommerceId))
                .isPresent();
    }

}
