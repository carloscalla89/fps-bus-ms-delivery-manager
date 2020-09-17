package com.inretailpharma.digital.deliverymanager.proxy;


import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.AssignedOrdersCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;

import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import reactor.core.publisher.Mono;

import java.util.List;


public interface OrderExternalService {

    Mono<Void> sendOrderReactive(OrderCanonical orderCanonical);
    Mono<Void> updateOrderReactive(OrderCanonical orderCanonical);
    Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company);
    Mono<OrderCanonical> getResultfromSellerExternalServices(OrderInfoCanonical orderInfoCanonical);
    Mono<OrderCanonical> retrySellerCenterOrder(OrderDto orderDto);
    Mono<OrderCanonical> sendOrderToTracker(OrderCanonical orderCanonical);
    Mono<OrderCanonical> sendOrderEcommerce(IOrderFulfillment iOrderFulfillment,
                                            List<IOrderItemFulfillment> itemFulfillments, String action);
    Mono<AssignedOrdersCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical);
    Mono<String> unassignOrders(UnassignedCanonical unassignedCanonical);
    Mono<String> updateOrderStatus(Long ecommerceId, String status);

}
