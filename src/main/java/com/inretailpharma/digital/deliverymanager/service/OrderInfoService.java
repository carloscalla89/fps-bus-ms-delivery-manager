package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.OrderInfoConsolidated;
import reactor.core.publisher.Mono;

public interface OrderInfoService {

   Mono<OrderInfoConsolidated> findOrderInfoClientByEcommerceId(long ecommerceId);

}
