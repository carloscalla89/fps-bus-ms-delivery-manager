package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.proxy.OrderTrackerServiceImpl;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class OrderTrackerAdapter extends AdapterAbstractUtil implements ITrackerAdapter {

    private OrderExternalService orderTrackerExternalService;

    @Autowired
    public OrderTrackerAdapter(@Qualifier("orderTracker") OrderExternalService orderTrackerExternalService) {

        this.orderTrackerExternalService = orderTrackerExternalService;
    }

    @Override
    public Mono<OrderCanonical> createOrderToTracker(Class<?> classImplement, StoreCenterCanonical store,
                                                     Long ecommerceId, Long externalId, String statusName,
                                                     String orderCancelCode, String orderCancelDescription,
                                                     String orderCancelObservation, String statusDetail,
                                                     ActionDto actionDto) {

        return orderTrackerExternalService
                .sendOrderToOrderTracker(getOrderFromIOrdersProjects(ecommerceId), actionDto);
    }

    @Override
    public Mono<OrderCanonical> updateOrderToTracker(Class<?> classImplement, ActionDto actionDto, Long ecommerceId,
                                                     String company, String serviceType, String orderCancelDescription) {

        return orderTrackerExternalService.updateOrderStatus(ecommerceId, actionDto);
    }

    public Flux<OrderCanonical> unassignOrders(UnassignedCanonical unassignedCanonical) {

        return orderTrackerExternalService
                    .unassignOrders(unassignedCanonical)
                    .filter(Constant.OrderTrackerResponseCode.SUCCESS_CODE::equals)
                    .flux()
                    .flatMap(statusCode -> {
                        log.info("#unassing orders from group {} - external tracker - statusCode success: {}"
                                , unassignedCanonical.getGroupName(), statusCode);

                        return Flux
                                .fromIterable(unassignedCanonical.getOrders())
                                .flatMap(ecommerceId -> {

                                    OrderCanonical orderCanonical = new OrderCanonical();
                                    orderCanonical.setEcommerceId(ecommerceId);

                                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                    orderStatus.setCode(Constant.OrderStatus.PREPARED_ORDER.getCode());
                                    orderStatus.setName(Constant.OrderStatus.PREPARED_ORDER.name());

                                    orderCanonical.setOrderStatus(orderStatus);

                                    return Flux.just(orderCanonical);

                                }).switchIfEmpty(Flux.defer(() -> {
                                    log.error("#unassing orders from group {} - external tracker - statusCode ERROR: {}"
                                            , unassignedCanonical.getGroupName(), statusCode);

                                    return Flux
                                            .fromIterable(unassignedCanonical.getOrders())
                                            .flatMap(ecommerceId -> {

                                                OrderCanonical orderCanonical = new OrderCanonical();
                                                orderCanonical.setEcommerceId(ecommerceId);

                                                OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                                orderStatus.setCode(Constant.OrderStatus.ERROR_PREPARED.getCode());
                                                orderStatus.setName(Constant.OrderStatus.ERROR_PREPARED.name());
                                                orderStatus.setDetail("Error when the orders have been unassigned");
                                                orderCanonical.setOrderStatus(orderStatus);

                                                return Flux.just(orderCanonical);

                                            });
                                }));

                    });

    }
}
