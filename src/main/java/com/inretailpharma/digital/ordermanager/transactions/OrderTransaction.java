package com.inretailpharma.digital.ordermanager.transactions;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.entity.*;
import com.inretailpharma.digital.ordermanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.ordermanager.service.OrderRepositoryService;
import com.inretailpharma.digital.ordermanager.util.Constant;
import com.inretailpharma.digital.ordermanager.util.DateUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
@Component
public class OrderTransaction {

    private OrderRepositoryService orderRepositoryService;

    public OrderTransaction(OrderRepositoryService orderRepositoryService) {
        this.orderRepositoryService = orderRepositoryService;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public OrderFulfillment createOrder(OrderFulfillment orderFulfillment, OrderDto orderDto) {

        OrderFulfillment orderFulfillmentResp = orderRepositoryService.createOrder(orderFulfillment, orderDto);

        // Set Object ServiceLocalOrderIdentity
        ServiceLocalOrderIdentity serviceLocalOrderIdentity = new ServiceLocalOrderIdentity();
        serviceLocalOrderIdentity.setLocalCode(orderDto.getLocalCode());
        serviceLocalOrderIdentity.setOrderTrackerId(orderFulfillmentResp.getId());
        serviceLocalOrderIdentity.setServiceTypeCode(orderDto.getServiceTypeCode());

        // status from delivery dispatcher
        setStatusOrderFromDeliveryDispatcher(serviceLocalOrderIdentity, orderDto);
        // ----------------------------------------------------

        // Create and set object ServiceLocalOrder
        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();
        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);
        serviceLocalOrder.setDaysToPickup(0);
        serviceLocalOrder.setStartHour(DateUtils.getLocalTimeFromStringWithFormat("09:00:00"));
        serviceLocalOrder.setEndHour(DateUtils.getLocalTimeFromStringWithFormat("20:00:00"));
        Optional
                .ofNullable(orderDto.getOrderStatusDto())
                .ifPresent(r -> serviceLocalOrder.setStatusDetail(r.getDescription()));


        orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);


        return orderFulfillmentResp;
    }

    public List<IOrderFulfillment> getListOrdersByStatus(Set<String> status){
        return orderRepositoryService.getListOrdersByStatus(status);
    }

    private void setStatusOrderFromDeliveryDispatcher(ServiceLocalOrderIdentity serviceLocalOrderIdentity,
                                                      OrderDto orderDto) {
        // set status
        if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId() != null) {
            serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.SUCCESS_TRACKED_BILLED_ORDER.getCode());

        } else if (
                Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1") && orderDto.getTrackerId() != null) {

            serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.getCode());

        } else if (
                !Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1")  && orderDto.getTrackerId() != null) {

            serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode());

        } else if (orderDto.getExternalPurchaseId() != null){
            serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());

        } else if (orderDto.getTrackerId() != null) {
            serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        } else {

            serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
        }
    }

}
