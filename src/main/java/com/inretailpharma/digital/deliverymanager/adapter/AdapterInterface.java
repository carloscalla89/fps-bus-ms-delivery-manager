package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import reactor.core.publisher.Mono;

public interface AdapterInterface {

    Mono<OrderCanonical> sendOrderTracker(OrderExternalService orderExternalService, StoreCenterCanonical storeCenter,
                                          Long ecommercePurchaseId, Long externalId, String statusDetail,
                                          String statusName, String orderCancelCode,
                                          String orderCancelObservation, String orderCancelAppType);

    Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService, Long ecommerceId,
                                                       ActionDto actionDto, String company, String serviceType,
                                                       Long orderId, String orderCancelCode,
                                                       String orderCancelObservation, String orderCancelAppType,
                                                       String statusCode, String origin);

    Mono<Void> createExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical);
    Mono<Void> updateExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical);
    Mono<Boolean> sendNotification(String channel, String serviceShortCode, String orderStatus, Long ecommerceId,
                                   String brand, String localCode, String localTypeCode, String phoneNumber,
                                   String clientName, String expiredDate, String confirmedDate, String address);




}
