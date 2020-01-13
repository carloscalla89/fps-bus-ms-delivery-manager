package com.inretailpharma.digital.deliverymanager.events;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.service.IConsumerService;
import com.inretailpharma.digital.deliverymanager.service.IPublisherService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class KafkaEvent {

    @Qualifier("kafkaPublisherService")
    private IPublisherService iPublisherService;

    private IConsumerService iConsumerService;

    public KafkaEvent(IPublisherService iPublisherService, IConsumerService iConsumerService) {
        this.iPublisherService = iPublisherService;
        this.iConsumerService = iConsumerService;
    }

    public void updateStatusOrder(OrderDto orderDto) {

        iPublisherService.sendOrder(orderDto);

    }

}
