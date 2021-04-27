package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.HistorySynchronizedDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface OrderFacadeProxy {

    Mono<OrderCanonical> createOrderToTracker(Long orderId, Long ecommerceId, Long externalId, String classImplement,
                                              String statusDetail, String statusName, String orderCancelCode,
                                              String orderCancelDescription, String orderCancelObservation,
                                              StoreCenterCanonical store, String source, boolean sendNewAudit,
                                              String updateBy);

    Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                            String statusDetail, String statusName, String orderCancelCode,
                                            String orderCancelDescription, String orderCancelObservation,
                                            String updateBy);

    Mono<OrderCanonical> sendOrderToTrackerFromRetryDD(IOrderFulfillment iOrderFulfillment, Long externalId,
                                                       String statusDetail, String statusName, String orderCancelCode,
                                                       String orderCancelDescription, String orderCancelObservation,
                                                       String updateBy);

    Mono<OrderCanonical> sendToUpdateOrder(IOrderFulfillment iOrderFulfillment, ActionDto actionDto);

    Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long id, Long ecommerceId, Long externalId,
                                          String orderCancelCode, String orderCancelObservation, String source,
                                          String target, boolean sendNewAudit, String updateBy, String actionDate);

    Mono<OrderCanonical> sendOnlyLastStatusOrderFromSync(IOrderFulfillment iOrdersFulfillment, ActionDto actionDto);

    Mono<OrderCanonical> updateOrderStatusListAudit(IOrderFulfillment iOrdersFulfillment, OrderCanonical orderSend,
                                                    HistorySynchronizedDto historySynchronized, String origin);

    void createExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical);

    Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localcode);

    Mono<OrderCanonical> getfromOnlinePaymentExternalServices(Long orderId, Long ecommercePurchaseId, String source,
                                                              String serviceTypeShortCode, String companyCode,
                                                              ActionDto actionDto);

    Mono<Boolean> processSendNotification(ActionDto actionDto, IOrderFulfillment iOrderFulfillment);

}
