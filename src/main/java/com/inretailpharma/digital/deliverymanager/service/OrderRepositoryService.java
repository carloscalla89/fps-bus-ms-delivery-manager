package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;

import java.util.List;
import java.util.Set;

public interface OrderRepositoryService {

    OrderFulfillment createOrder(OrderFulfillment orderFulfillment, OrderDto orderDto);
    ServiceType getServiceTypeByCode(String code);
    Local getLocalByCode(String code);
    Local getLocalByLocalCodeAndCompanyCode(String localCode, String companyCode);
    OrderStatus getOrderStatusByCode(String code);
    ServiceLocalOrder saveServiceLocalOrder(ServiceLocalOrder serviceLocalOrder);
    List<IOrderFulfillment> getListOrdersByStatus(Set<String> status);
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
}
