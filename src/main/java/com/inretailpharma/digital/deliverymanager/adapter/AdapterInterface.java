package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AdapterInterface {

    Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService, Long ecommerceId,
                                                       ActionDto actionDto, String company, String serviceType,
                                                       Long orderId, String orderCancelCode,
                                                       String orderCancelDescription, String orderCancelObservation,
                                                       String statusCode, String origin);

    Mono<Void> updateExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical, String updateBy);
    Mono<Boolean> sendNotification(String channel, String serviceShortCode, String orderStatus, Long ecommerceId,
                                   String brand, String localCode, String localTypeCode, String phoneNumber,
                                   String clientName, String expiredDate, String confirmedDate, String address);

    Mono<OrderCanonical> getOrder(IOrderFulfillment iOrderFulfillment);




}
