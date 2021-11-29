package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import reactor.core.publisher.Mono;

public interface ISellerCenterAdapter {

    Mono<OrderCanonical> updateStatusOrderSeller(Long ecommerceId, String actionName);
}
