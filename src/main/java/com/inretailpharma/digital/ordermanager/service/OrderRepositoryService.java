package com.inretailpharma.digital.ordermanager.service;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.entity.*;
import com.inretailpharma.digital.ordermanager.entity.projection.IOrderFulfillment;

import java.util.List;
import java.util.Set;

public interface OrderRepositoryService {

    OrderFulfillment createOrder(OrderFulfillment orderFulfillment, OrderDto orderDto);
    ServiceType getByCode(String code);
    Local getByLocalCode(String localCode);
    ServiceLocalOrder saveServiceLocalOrder(ServiceLocalOrder serviceLocalOrder);
    List<IOrderFulfillment> getListOrdersByStatus(Set<String> status);
}
