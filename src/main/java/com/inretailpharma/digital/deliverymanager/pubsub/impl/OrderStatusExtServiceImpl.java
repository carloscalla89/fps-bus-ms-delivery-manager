package com.inretailpharma.digital.deliverymanager.pubsub.impl;

import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import com.inretailpharma.digital.deliverymanager.pubsub.OrderStatusService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;

@Slf4j
@ConditionalOnProperty(value="spring.cloud.gcp.project-id")
@EnableBinding(OrderStatusService.class)
public class OrderStatusExtServiceImpl {

    private DeliveryManagerFacade deliveryManagerFacade;

    public OrderStatusExtServiceImpl(DeliveryManagerFacade deliveryManagerFacade) {
        this.deliveryManagerFacade = deliveryManagerFacade;;
    }

    @StreamListener(OrderStatusService.CHANNEL)
    public void handleMessage(ActionDto payload) {

        log.info("[START] OrderStatusServiceImpl - payload {}", payload);
        
        if (payload != null) {


	        var result = deliveryManagerFacade
	                .getUpdateOrder(payload, String.valueOf(payload.getEcommerceId()), false)
	                .block();        
    

        	log.info("[INFO] OrderStatusServiceImpl - result {}", result);
    	}
        
        log.info("[END] OrderStatusServiceImpl - payload {}", payload);

    }

}
