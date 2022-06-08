package com.inretailpharma.digital.deliverymanager.adapter;

import java.util.List;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.HistorySynchronizedDto;

import reactor.core.publisher.Mono;

public interface ISellerCenterAdapter {

    Mono<OrderCanonical> updateStatusOrderSeller(Long ecommerceId, String actionName);
    Mono<OrderCanonical> updateListStatusOrderSeller(Long ecommerceId, List<HistorySynchronizedDto> history);
}
