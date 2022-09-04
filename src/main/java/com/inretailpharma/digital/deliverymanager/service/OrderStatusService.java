package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;

import java.util.List;

public interface OrderStatusService {

  List<OrderStatusDto> getAllOrderStatus();

  OrderStatusDto findById(String id);
}
