package com.inretailpharma.digital.deliverymanager.facade;

import java.util.*;
import java.util.stream.Collectors;

import com.inretailpharma.digital.deliverymanager.adapter.*;
import com.inretailpharma.digital.deliverymanager.strategy.IActionStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class DeliveryManagerFacade extends FacadeAbstractUtil {

    private OrderTransaction orderTransaction;
    private IAuditAdapter iAuditAdapter;
    private LiquidationFacade liquidationFacade;
    private Map<Constant.ActionOrder, IActionStrategy> actionsProcessors;
    private ApplicationContext context;

    @Autowired
    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 @Qualifier("auditAdapter") IAuditAdapter iAuditAdapter,
                                 LiquidationFacade liquidationFacade,
                                 ApplicationContext context) {

        this.orderTransaction = orderTransaction;
        this.iAuditAdapter = iAuditAdapter;
        this.liquidationFacade = liquidationFacade;
        this.context = context;

        actionsProcessors = Arrays
                                .stream(Constant.ActionOrder.values())
                                .collect(Collectors.toMap(p -> p, p ->
                                        (IActionStrategy)this.context.getBean(p.getActionStrategyImplement())));
    }

    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {

        return createOrderFulfillment(orderDto);

    }

    public Mono<OrderCanonical> getUpdateOrder(ActionDto actionDto, String ecommerceId) {
        log.info("[START] getUpdateOrder action:{}", actionDto);

        IActionStrategy actionStrategy = actionsProcessors.get(Constant.ActionOrder.getByName(actionDto.getAction()));

        Objects.requireNonNull(actionStrategy, "No processor for " + actionDto.getAction() + " found");

        return Mono
                .fromCallable(() -> actionStrategy.evaluate(actionDto, ecommerceId).subscribeOn(Schedulers.boundedElastic()))
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

    public Mono<OrderCanonical> getUpdatePartialOrder(OrderDto partialOrderDto) {
        log.info("[START] getUpdatePartialOrder:{}",partialOrderDto);

        return Mono
                .just(orderTransaction.updatePartialOrder(partialOrderDto))
                .flatMap(order -> {
                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.PARTIAL_UPDATE_ORDER.getCode());
                    orderStatus.setName(Constant.OrderStatus.PARTIAL_UPDATE_ORDER.getCode());
                    order.setOrderStatus(orderStatus);

                    return Mono.just(order);

                })
                .onErrorResume(e -> {

                    e.printStackTrace();
                    log.error("Error during update partial order:{}",e.getMessage());

                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setEcommerceId(partialOrderDto.getEcommercePurchaseId());

                    OrderStatusCanonical statusCanonical = new OrderStatusCanonical();
                    statusCanonical.setCode(Constant.OrderStatus.ERROR_PARTIAL_UPDATE.getCode());
                    statusCanonical.setName(Constant.OrderStatus.ERROR_PARTIAL_UPDATE.name());
                    statusCanonical.setDetail(e.getMessage());

                    return Mono.just(orderCanonical);

                })
                .flatMap(order -> iAuditAdapter.updateAudit(order, Constant.UPDATED_BY_INKATRACKER_WEB))
                .flatMap(order -> liquidationFacade.evaluateUpdate(order));
    }

}