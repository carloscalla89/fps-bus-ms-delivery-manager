package com.inretailpharma.digital.ordermanager.facade;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.service.IPublisherService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OrderProcessFacade {

    @Qualifier("kafkaPublisherService")
    private IPublisherService iPublisherService;

    public OrderProcessFacade(IPublisherService iPublisherService) {
        this.iPublisherService = iPublisherService;
    }

    public void updateStatusOrder(OrderDto orderDto) {

        iPublisherService.sendOrder(orderDto);

    }

}
