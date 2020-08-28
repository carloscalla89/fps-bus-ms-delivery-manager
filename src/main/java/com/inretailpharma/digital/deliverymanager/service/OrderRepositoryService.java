package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;

import java.util.List;
import java.util.Set;

public interface OrderRepositoryService {

    OrderFulfillment createOrder(OrderFulfillment orderFulfillment);
    ServiceType getServiceTypeByCode(String code);
    OrderStatus getOrderStatusByCode(String code);
    ServiceLocalOrder saveServiceLocalOrder(ServiceLocalOrder serviceLocalOrder);
    List<IOrderFulfillment> getListOrdersByStatus(Set<String> status);
    List<IOrderFulfillment> getListOrdersToCancel(String serviceType,String companyCode, Integer maxDayPickup, String statustype);
    List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderFulfillmentId);
    IOrderFulfillment getOrderByecommerceId(Long ecommerceId);
    void updateRetryingOrderStatusProcess(Long orderFulfillmentId, Integer attemptTracker,
                                   Integer attempt, String orderStatusCode, String statusDetail);
    void updateReattemtpTracker(Long orderFulfillmentId, Integer attemptTracker,
                               String orderStatusCode, String statusDetail);
    void updateTrackerId(Long orderFulfillmentId, Long trackerId);
    void updateExternalAndTrackerId(Long orderFulfillmentId, Long externalPurchaseId, Long trackerId);

    void updateExternalIdToReservedOrder(Long orderFulfillmentId, Long externalPurchaseId);
    void updateStatusToReservedOrder(Long orderFulfillmentId, Integer attempt, String orderStatusCode,
                                     String statusDetail);
    void updateStatusOrder(Long orderFulfillmentId, String orderStatusCode, String statusDetail);

    void updateStatusCancelledOrder(String statusDetail, String cancellationObservation, String cancellationCode,
                                    String cancellationAppType, String orderStatusCode, Long orderFulfillmentId);

    Client saveClient(Client client);

    List<OrderStatus> getOrderStatusByTypeIs(String statusName);

    boolean updatePartialOrderHeader(OrderDto orderDto);
    boolean updatePartialOrderDetail(OrderDto orderDto, List<IOrderItemFulfillment> iOrderItemFulfillment);
    boolean deleteItemRetired(String itemId, Long orderFulFillmentId);

    void updatePaymentMethod(OrderDto partialOrderDto, Long orderFulfillmentId);
}
