package com.inretailpharma.digital.deliverymanager.proxy;


import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.AssignedOrdersCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.LiquidationDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.dto.notification.MessageDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import reactor.core.publisher.Mono;

import java.util.List;


public interface OrderExternalService {

    Mono<Void> updateOrderReactive(OrderCanonical orderCanonical);
    Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company,
                                                       String serviceType, String cancelDescription);



    Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                            List<IOrderItemFulfillment> itemFulfillments,
                                            StoreCenterCanonical storeCenterCanonical, Long externalId, String statusDetail,
                                            String statusName, String orderCancelCode, String orderCancelDescription,
                                            String orderCancelObservation);

    Mono<OrderCanonical> createOrderToLiquidation(LiquidationDto liquidationDto);
    Mono<OrderCanonical> updateOrderToLiquidation(String ecommerceId, StatusDto statusDto);
    Mono<OrderCanonical> updateOrderToLiquidationOnline(String ecommerceId, StatusDto statusDto);

    Mono<AssignedOrdersCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical);
    Mono<String> unassignOrders(UnassignedCanonical unassignedCanonical);
    Mono<OrderCanonical> updateOrderStatus(Long ecommerceId, ActionDto actionDto);
    Mono<OrderCanonical> getResultfromOnlinePaymentExternalServices(Long ecommercePurchaseId, String source,
                                                                    String serviceTypeShortCode, String companyCode,
                                                                    ActionDto actionDto);
    Mono<String> addControversy(ControversyRequestDto controversyRequestDto, Long ecommerceId);
    Mono<OrderCanonical> sendOrderToOrderTracker(OrderCanonical orderCanonical, ActionDto actionDto);
    Mono<Void> updateOrderNewAudit(AuditHistoryDto orderCanonical);
    Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localcode);

    Mono<Void> sendOrderReactive(OrderCanonical orderAuditCanonical);

    Mono<Void> sendNotification(MessageDto messageDto);

    Mono<Void> createOrderNewAudit(AuditHistoryDto auditHistoryDto);
    
    Mono<Void> releaseStock(Long externalId);

    Mono<Void> updateStatusOrderSeller(Long externalId, String status);

    Mono<Void> updatePartial(OrderDto partialOrderDto);
}
