package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.repository.OrderStatusRepository;
import com.inretailpharma.digital.deliverymanager.service.OrderStatusService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

  private ObjectToMapper objectToMapper;
  private OrderStatusRepository orderStatusRepository;

  @Override
  public List<OrderStatusDto> getAllOrderStatus() {
    return objectToMapper.getOrderStatusDto(orderStatusRepository.findAll());
  }

  @Override
  public OrderStatusDto findById(String id) {
    return objectToMapper.mapToOrderStatusDto(orderStatusRepository.findById(id).orElse(new OrderStatus()));
  }
}
