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

        //ServiceType serviceType = orderRepositoryService.getByCode(orderDto.getServiceTypeCode());
        //Local local = orderRepositoryService.getByLocalCode(orderDto.getLocalCode());

        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();
        ServiceLocalOrderIdentity serviceLocalOrderIdentity = new ServiceLocalOrderIdentity();
        serviceLocalOrderIdentity.setLocalCode(orderDto.getLocalCode());
        serviceLocalOrderIdentity.setOrderTrackerId(orderFulfillmentResp.getId());
        serviceLocalOrderIdentity.setServiceTypeCode(orderDto.getServiceTypeCode());

        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);
        serviceLocalOrder.setAttempt(1);
        serviceLocalOrder.setDaysToPickup(0);
        serviceLocalOrder.setStartHour(DateUtils.getLocalTimeFromStringWithFormat("09:00:00"));
        serviceLocalOrder.setEndHour(DateUtils.getLocalTimeFromStringWithFormat("20:00:00"));

        orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);

        return orderFulfillmentResp;
    }

    public List<IOrderFulfillment> getListOrdersByStatus(Set<String> status){
        return orderRepositoryService.getListOrdersByStatus(status);
    }

}