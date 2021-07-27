package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import reactor.core.publisher.Mono;

public interface IInsinkAdapter {

    Mono<OrderDto> getOrderEcommerce(Long ecommerceId);
}
