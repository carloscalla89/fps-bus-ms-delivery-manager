package com.inretailpharma.digital.deliverymanager.factory;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.service.IConsumerService;
import com.inretailpharma.digital.deliverymanager.service.IPublisherService;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessKakfa extends OrderProcess {

    public OrderProcessKakfa(IPublisherService iPublisherService,
                             IConsumerService iConsumerService) {
        super(iPublisherService, iConsumerService);
    }

    @Override
    public void create() {
        // aca se implementaría lo de kafka
    }

    @Override
    public void send(OrderDto orderDto) {

        iPublisherService.sendOrder(orderDto);
    }

/*
    @Override
    public void receiver(OrderDto orderDto, Acknowledgment acknowledgment) {
        iConsumerService.receiverOrderCallback(orderDto, acknowledgment);
    }
*/

}
