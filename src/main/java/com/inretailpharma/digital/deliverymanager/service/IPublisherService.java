package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;

public interface IPublisherService {

    void sendOrder(OrderDto orderDto);

    void sendOrderCallBack(OrderDto orderDto);


}
