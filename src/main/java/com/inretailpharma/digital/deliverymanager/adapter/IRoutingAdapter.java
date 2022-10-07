package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;

import reactor.core.publisher.Mono;

public interface IRoutingAdapter {
	
	Mono<OrderCanonical> createOrder(OrderCanonical orderCanonical);
	Mono<OrderCanonical> cancelOrder(Long orderId, boolean externalRouting, String serviceType, String action, String origin);

}
