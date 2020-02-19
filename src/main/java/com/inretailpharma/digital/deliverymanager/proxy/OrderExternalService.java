package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import reactor.core.publisher.Mono;


public interface OrderExternalService {

    Mono<OrderCanonical> sendOrderReactive(OrderCanonical orderCanonical);
    Mono<OrderCanonical> sendOrderReactiveWithOrderDto(OrderCanonical orderCanonical);
    Mono<OrderCanonical> updateOrderReactive(OrderCanonical orderCanonical);
    OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto);

}
