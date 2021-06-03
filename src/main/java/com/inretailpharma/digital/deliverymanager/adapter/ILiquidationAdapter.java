package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import reactor.core.publisher.Mono;

public interface ILiquidationAdapter {

    Mono<OrderCanonical> createOrder(OrderCanonical completeOrder, OrderCanonical orderProcess);

    Mono<OrderCanonical> updateOrder(OrderCanonical orderCanonical, String liquidationStatus);
}
