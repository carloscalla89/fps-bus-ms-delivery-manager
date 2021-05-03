package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import reactor.core.publisher.Mono;

public interface ILiquidationAdapter {

    Mono<OrderCanonical> createOrder(OrderCanonical orderCanonical);

    Mono<OrderCanonical> updateOrder(OrderCanonical orderCanonical, String action);
}
