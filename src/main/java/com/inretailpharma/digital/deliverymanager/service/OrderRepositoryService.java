package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.inretailpharma.digital.deliverymanager.entity.Client;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.deliverymanager.entity.ServiceType;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import reactor.core.publisher.Flux;

public interface OrderRepositoryService {

    OrderFulfillment createOrder(OrderFulfillment orderFulfillment);
    ServiceType getServiceTypeByCode(String code);
    OrderStatus getOrderStatusByCode(String code);
    ServiceLocalOrder saveServiceLocalOrder(ServiceLocalOrder serviceLocalOrder);
    List<IOrderFulfillment> getListOrdersToCancel(String serviceType,String companyCode, Integer maxDayPickup, String statustype);
    List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderFulfillmentId);
    IOrderFulfillment getOrderByecommerceId(Long ecommerceId);
    IOrderFulfillment getOrderLightByecommerceId(Long ecommerceId);
    List<IOrderFulfillment> getOrderLightByecommercesIds(Set<Long> ecommercesIds);
    List<IOrderFulfillment> getOrdersByEcommerceIds(Set<Long> ecommercesIds);

    void updateStatusCancelledOrder(String statusDetail, String cancellationObservation, String cancellationCode,
                                    String orderStatusCode, Long orderFulfillmentId, LocalDateTime updateLast,
                                    LocalDateTime dateCancelled);

    void updateStatusOrder(String statusDetail, String orderStatusCode, Long ecommerceId, LocalDateTime updateLast);

    void updateLiquidationStatusOrder(String liquidationStatusDetail, String liquidaitonStatus, Long orderFulfillmentId);

    Client saveClient(Client client);

    IOrderFulfillment getOnlyOrderStatusByecommerceId(Long ecommerceId);

    <T> Optional<IOrderResponseFulfillment> getOrderByOrderNumber(Long orderNumber);

    List<IOrderFulfillment> getOrder();

    boolean updatePartialOrderHeader(OrderDto orderDto);
    boolean updatePartialOrderDetail(OrderDto orderDto, List<IOrderItemFulfillment> iOrderItemFulfillment);
    boolean deleteItemRetired(String itemId, Long orderFulFillmentId);
    void updatePaymentMethod(OrderDto partialOrderDto, Long orderFulfillmentId);

    void updateOnlinePaymentStatusByOrderId(Long orderId, String onlinePaymentStatus);
}
