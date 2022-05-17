package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.util.Constant.OrderStatus;
import java.util.List;
import reactor.core.publisher.Flux;

public interface OrderStatusService {

  List<OrderStatusDto> getAllOrderStatus();

}
