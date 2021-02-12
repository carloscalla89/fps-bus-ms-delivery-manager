package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import reactor.core.publisher.Mono;

public interface AdapterInterface {

    Mono<OrderCanonical> sendOrderTracker(OrderExternalService orderExternalService, Long ecommercePurchaseId,
                                          Long externalId, String statusDetail, String statusName, String orderCancelCode,
                                          String orderCancelObservation, String orderCancelAppType);

    Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService, Long ecommerceId,
                                                       ActionDto actionDto, String company, String serviceType,
                                                       Long orderId, String orderCancelCode,
                                                       String orderCancelObservation, String orderCancelAppType);

    Mono<Void> updateExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical);



}
