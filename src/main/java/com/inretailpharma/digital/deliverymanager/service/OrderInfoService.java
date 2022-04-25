package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrdersSelectedResponse;
import com.inretailpharma.digital.deliverymanager.dto.FilterOrderDTO;
import com.inretailpharma.digital.deliverymanager.dto.OrderInfoConsolidated;
import reactor.core.publisher.Mono;

import java.util.List;

public interface OrderInfoService {

   Mono<OrderInfoConsolidated> findOrderInfoClientByEcommerceId(long ecommerceId);

   OrdersSelectedResponse getOrderHeaderDetails(FilterOrderDTO filter);
}
