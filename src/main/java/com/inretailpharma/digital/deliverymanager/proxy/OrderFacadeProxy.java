package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import reactor.core.publisher.Mono;

public interface OrderFacadeProxy {

    Mono<OrderCanonical> sendOrderToTracker(Long orderId, Long ecommerceId, Long externalId, String serviceTypeCode,
                                            String statusDetail, String statusName, String orderCancelCode,
                                            String orderCancelObservation);

    Mono<OrderCanonical> sendToUpdateOrder(Long orderId, Long ecommerceId, Long externalId, ActionDto actionDto,
                                           String serviceType, String serviceTypeCode, String source,
                                           String companyCode, String localCode, String statusCode,
                                           boolean sendToUpdateOrder);

    Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long id, Long ecommerceId, Long externalId,
                                          String orderCancelCode, String orderCancelObservation, String orderCancelAppType);
}
