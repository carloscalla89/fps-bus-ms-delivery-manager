package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import reactor.core.publisher.Mono;

public interface IDeliveryDispatcherAdapter {

    Mono<OrderCanonical> sendRetryInsink(Long ecommerceId, String companyCode, StoreCenterCanonical store);

    Mono<OrderDto> getOrderEcommerce(Long ecommerceId, String companyCode);

}
