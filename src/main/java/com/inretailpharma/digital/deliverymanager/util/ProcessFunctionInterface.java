package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ProcessFunctionInterface {

    Mono<OrderCanonical> getMapOrderCanonical(Long ecommerceId, String action, String errorDetail,
                                              String firstOrderStatusName, Long orderId, String serviceType);

}
