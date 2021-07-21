package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.GroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.List;

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
                .sendOrderToOrderTracker(getOrderToOrderTracker(ecommerceId), actionDto);
    }

    @Override
    public Mono<OrderCanonical> updateOrderToTracker(Class<?> classImplement, ActionDto actionDto, Long ecommerceId,
                                                     String company, String serviceType, String orderCancelDescription) {

        return orderTrackerExternalService.updateOrderStatus(ecommerceId, actionDto);
    }


    public Flux<OrderCanonical> assignOrders(ProjectedGroupCanonical newProjectedGroupCanonical,
                                             List<GroupCanonical> groupCanonicals) {

        log.info("[START] Sending orders to OT");

        orderTrackerExternalService
                .assignOrders(newProjectedGroupCanonical)
                .subscribe(resp -> log.info("[END] Sending order to OT with response:{}",resp));

        return Flux
                .fromIterable(groupCanonicals)
                .flatMap(ecommerceId -> {

                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setEcommerceId(ecommerceId.getOrderId());

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.ASSIGNED.getCode());
                    orderStatus.setName(Constant.OrderStatus.ASSIGNED.name());
                    orderStatus.setDetail("The order was sent to order tracker");
                    orderCanonical.setOrderStatus(orderStatus);

                    return Flux.just(orderCanonical);

                });

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

                                });

                    })
                    .switchIfEmpty(Flux.defer(() -> {
                        log.error("#unassing orders from group {} - external tracker - statusCode ERROR",
                                unassignedCanonical.getGroupName());

                        return Flux
                                .fromIterable(unassignedCanonical.getOrders())
                                .flatMap(ecommerceId -> {

                                    OrderCanonical orderCanonical = new OrderCanonical();
                                    orderCanonical.setEcommerceId(ecommerceId);

                                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                    orderStatus.setCode(Constant.OrderStatus.ERROR_PREPARED.getCode());
                                    orderStatus.setName(Constant.OrderStatus.ERROR_PREPARED.name());
                                    orderStatus.setDetail("The response is empty when the orders have been unassigned");
                                    orderCanonical.setOrderStatus(orderStatus);

                                    return Flux.just(orderCanonical);

                                });
                    }))
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        log.error("#unassing orders from group {} - external tracker - statusCode ERROR:{}",
                                unassignedCanonical.getGroupName(),e.getMessage());

                        return Flux
                                .fromIterable(unassignedCanonical.getOrders())
                                .flatMap(ecommerceId -> {

                                    OrderCanonical orderCanonical = new OrderCanonical();
                                    orderCanonical.setEcommerceId(ecommerceId);

                                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                    orderStatus.setCode(Constant.OrderStatus.ERROR_PREPARED.getCode());
                                    orderStatus.setName(Constant.OrderStatus.ERROR_PREPARED.name());
                                    orderStatus.setDetail("The response is failed when the orders have been unassigned " +
                                            "- detail:" + e.getMessage());
                                    orderCanonical.setOrderStatus(orderStatus);

                                    return Flux.just(orderCanonical);

                                });

                    });

    }
}
