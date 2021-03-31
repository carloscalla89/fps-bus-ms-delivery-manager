package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface OrderFacadeProxy {

    Mono<OrderCanonical> createOrderToTracker(Long orderId, Long ecommerceId, Long externalId, String classImplement,
                                              String statusDetail, String statusName, String orderCancelCode,
                                              String orderCancelDescription, String orderCancelObservation,
                                              StoreCenterCanonical store, String source, boolean sendNewAudit,
                                              String updateBy);

    Mono<OrderCanonical> sendOrderToTracker(Long orderId, Long ecommerceId, Long externalId, String classImplement,
                                            String statusDetail, String statusName, String orderCancelCode,
                                            String orderCancelDescription, String orderCancelObservation,
                                            String companyCode, String localCode, String source, boolean sendNewAudit,
                                            String updateBy, com.inretailpharma.digital.deliverymanager.dto.OrderDto orderDto);

    Mono<OrderCanonical> sendToUpdateOrder(Long orderId, Long ecommerceId, Long externalId, ActionDto actionDto,
                                           String serviceType, String serviceShortCode, String classImplementTracker,
                                           String source, String channel, String companyCode, String localCode,
                                           String statusCode, String clientName, String phone, LocalDateTime scheduledTime,
                                           boolean sendNewAudit, boolean sendNotificationByChannel);

    Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long id, Long ecommerceId, Long externalId,
                                          String orderCancelCode, String orderCancelObservation, String source,
                                          String target, boolean sendNewAudit, String updateBy, String actionDate);

    void createExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical);

    Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localcode);

    Mono<OrderCanonical> getfromOnlinePaymentExternalServices(Long orderId, Long ecommercePurchaseId, String source,
                                                              String serviceTypeShortCode, String companyCode,
                                                              ActionDto actionDto);
}
