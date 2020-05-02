package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Slf4j
@Service("orderTracker")
public class OrderTrackerServiceImpl extends AbstractOrderService  implements OrderExternalService {

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
    	log.info("[START] service to call api to sendOrderToTracker - uri:{} - body:{}",
                externalServicesProperties.getOrderTrackerCreateOrderUri(), orderCanonical);
    	
		ResponseEntity<String> response = WebClient
        	.create(externalServicesProperties.getOrderTrackerCreateOrderUri())
        	.post()
        	.bodyValue(orderCanonical)
        	.retrieve()
        	.toEntity(String.class)
        	.block();
		
		log.info("[END] service to call api to sendOrderToTracker - s:{}", response.getBody());
		
        return Mono.empty();
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        return null;
    }
    
    @Override
	public Mono<Void> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {
    	log.info("[START] service to call api to assignOrders - uri:{} - body:{}",
                externalServicesProperties.getOrderTrackerAssignOrdersUri(), projectedGroupCanonical);
    	
    	ResponseEntity<String> response = WebClient
            	.create(externalServicesProperties.getOrderTrackerAssignOrdersUri())
            	.post()
            	.bodyValue(projectedGroupCanonical)
            	.retrieve()
            	.toEntity(String.class)
            	.block();
    	
    	log.info("[END] service to call api to assignOrders - s:{}", response.getBody());
    	return Mono.empty();
	}

	@Override
	public Mono<Void> unassignOrders(UnassignedCanonical unassignedCanonical) {
		log.info("[START] service to call api to unassignOrders - uri:{} - body:{}",
                externalServicesProperties.getOrderTrackerUnassignOrdersUri(), unassignedCanonical);
		
		ResponseEntity<String> response = WebClient
	        	.create(externalServicesProperties.getOrderTrackerUnassignOrdersUri())
	        	.patch()
	        	.bodyValue(unassignedCanonical)
	        	.retrieve()
	        	.toEntity(String.class)
	        	.block();
		
		log.info("[END] service to call api to unassignOrders - s:{}", response.getBody());		
		return Mono.empty();
	}
	
	@Override
	public Mono<Void> updateOrderStatus(Long ecommerceId, String status) {
		log.info("[START] service to call api to updateOrderStatus - uri:{} - ecommerceId:{} - status:{}",
                externalServicesProperties.getOrderTrackerUpdateOrderStatusUri(), ecommerceId, status);
		
		ResponseEntity<String> response = WebClient
				.builder()
                .baseUrl(externalServicesProperties.getOrderTrackerUpdateOrderStatusUri())
                .build()
                .patch()
                .uri(builder ->
                		builder
                				.path("/{ecommerceId}/status/{status}")
                                .build(ecommerceId, status))
                .retrieve()
                .toEntity(String.class)
            	.block();
		
		log.info("[END] service to call api to updateOrderStatus - s:{}", response.getBody());
		return Mono.empty();
	}
}
