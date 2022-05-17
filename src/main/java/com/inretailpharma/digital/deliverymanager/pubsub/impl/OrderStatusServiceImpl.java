package com.inretailpharma.digital.deliverymanager.pubsub.impl;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import com.inretailpharma.digital.deliverymanager.pubsub.OrderStatusService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import reactor.core.publisher.Mono;

@Slf4j
@EnableBinding(OrderStatusService.class)
public class OrderStatusServiceImpl {

    private DeliveryManagerFacade deliveryManagerFacade;

    public OrderStatusServiceImpl(DeliveryManagerFacade deliveryManagerFacade) {
        this.deliveryManagerFacade = deliveryManagerFacade;
    }

    @StreamListener(OrderStatusService.CHANNEL)
    public void handleMessage(ActionDto payload) {

        log.info("[START] OrderStatusServiceImpl - payload {}", payload);
        
        if (payload != null) {


	        var result = deliveryManagerFacade
	                .getUpdateOrder(payload, String.valueOf(payload.getEcommerceId()), false)
	                .onErrorResume(ex -> {
	
	                    OrderStatusCanonical os = new OrderStatusCanonical();
	                    os.setName("ERROR");
	                    os.setDetail(ex.getMessage());
	                    OrderCanonical order = new OrderCanonical();
	                    order.setOrderStatus(os);
	                    order.setEcommerceId(payload.getEcommerceId());
	                    return Mono.just(order);
	                })
	                .block();        
    

        	log.info("[INFO] OrderStatusServiceImpl - result {}", result);
    	}
        
        log.info("[END] OrderStatusServiceImpl - payload {}", payload);

    }

}
