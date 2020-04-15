package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service("orderTracker")
public class OrderTrackerServiceImpl implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;
    private ApplicationParameterService applicationParameterService;

    public OrderTrackerServiceImpl(ExternalServicesProperties externalServicesProperties,
                                   ApplicationParameterService applicationParameterService) {
        this.externalServicesProperties = externalServicesProperties;
        this.applicationParameterService = applicationParameterService;
    }

    @Override
    public Mono<Void> sendOrderReactive(OrderCanonical orderCanonical) {
        return null;

    }

    @Override
    public Mono<Void> updateOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<Void> sendOrderToTracker(OrderCanonical orderCanonical) {
    	log.info("[START] service to call api order-tracker to createOrder - uri:{} - body:{}",
                externalServicesProperties.getOrderTrackerCreateOrderUri(), orderCanonical);
    	
		ResponseEntity<String> response = WebClient
        	.create(externalServicesProperties.getOrderTrackerCreateOrderUri())
        	.post()
        	.bodyValue(orderCanonical)
        	.retrieve()
        	.toEntity(String.class)
        	.block();
		
		log.info("[END] service to call api order-tracker to createOrder - s:{}", response.getBody());
		
        return Mono.empty();
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        return null;
    }
}
