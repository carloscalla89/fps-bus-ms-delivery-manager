package com.inretailpharma.digital.deliverymanager.factory;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.service.IConsumerService;
import com.inretailpharma.digital.deliverymanager.service.IPublisherService;

public abstract class OrderProcess {

    public IPublisherService iPublisherService;
    public IConsumerService iConsumerService;

    public OrderProcess(IPublisherService iPublisherService, IConsumerService iConsumerService) {
        this.iPublisherService = iPublisherService;
        this.iConsumerService = iConsumerService;
    }

    public abstract void create();

    public abstract void send(OrderDto orderDto);
/*
    public abstract void receiver(OrderDto orderDto, Acknowledgment acknowledgment);


 */
}
