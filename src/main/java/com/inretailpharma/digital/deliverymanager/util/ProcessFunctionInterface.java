package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import reactor.core.publisher.Mono;

import java.util.List;

@FunctionalInterface
public interface ProcessFunctionInterface {

    Mono<OrderCanonical> getMapOrderCanonical(Long ecommerceId, String action, String errorDetail);

}
