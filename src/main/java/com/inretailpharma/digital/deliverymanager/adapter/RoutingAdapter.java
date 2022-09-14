package com.inretailpharma.digital.deliverymanager.adapter;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RoutingAdapter extends AdapterAbstractUtil implements IRoutingAdapter {
	
	private OrderExternalService routingService;
	private OrderExternalService auditService;
	private ObjectToMapper objectToMapper;
	private ApplicationParameterService applicationParameterService;
	
	public RoutingAdapter(@Qualifier("routing")OrderExternalService routingService,
			@Qualifier("audit") OrderExternalService auditService,
			ObjectToMapper objectToMapper,
			ApplicationParameterService applicationParameterService) {
		this.routingService = routingService;
		this.auditService = auditService;
		this.objectToMapper = objectToMapper;
		this.applicationParameterService = applicationParameterService;
	}

	@Override
	public Mono<OrderCanonical> createOrder(OrderCanonical orderCanonical) {
		
		Optional.ofNullable(orderCanonical.getStoreCenter())
			.ifPresent(sc -> {
				
				if (sc.isExternalRoutingEnabled()) {
					
					boolean routingEnabled = Optional
		                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ENABLED_EXTERNAL_ROUTING))
		                    .map(val -> Constant.Logical.getByValueString(val.getValue()).value())
		                    .orElse(false);
					
					if (routingEnabled) {
						
						int defaultVolume = Optional
			                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ROUTING_DEFAULT_VOLUME))
			                    .map(val -> Integer.parseInt(val.getValue()))
			                    .orElse(Constant.Routing.DEFAULT_VOLUME);
						
						int defaultDeliveryTime = Optional
			                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ROUTING_DEFAULT_DELIVERY_TIME))
			                    .map(val -> Integer.parseInt(val.getValue()))
			                    .orElse(Constant.Routing.DEFAULT_DELIVERY_TIME);
						
						
						Mono.fromCallable(() -> this.getOrderByEcommerceId(orderCanonical.getEcommerceId()))
						.flatMap(order -> 
							
							routingService.createOrderRouting(orderCanonical.getEcommerceId(),
									objectToMapper.convertIOrderFulfillmentToRoutedOrder(order, orderCanonical.getOrderItems().size(),
											defaultVolume, defaultDeliveryTime, sc.getExternalRoutingLocalCode()))
							.flatMap(a -> {
								a.setTarget(Constant.TARGET_ROUTING);						
								return auditService.updateOrderNewAudit(getAuditHistoryDtoFromObject(a, null));
								
							})
							
						).subscribe();	
						
					}					
					
				}
			});	

		
		return Mono.just(orderCanonical);
	}

	@Override
	public Mono<OrderCanonical> cancelOrder(Long orderId, boolean externalRouting, String action, String origin) {
		
		Optional.ofNullable(orderId)
			.ifPresent(sc -> {
				
				if (externalRouting &&
						(Constant.ActionOrder.CANCEL_ORDER.name().equals(action) || Constant.ActionOrder.REJECT_ORDER.name().equals(action)) &&
						!Constant.ORIGIN_ROUTING.equalsIgnoreCase(origin)
						) {
					
					boolean routingEnabled = Optional
		                    .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ENABLED_EXTERNAL_ROUTING))
		                    .map(val -> Constant.Logical.getByValueString(val.getValue()).value())
		                    .orElse(false);
					
					if (routingEnabled) {
						
						routingService.updateOrderRouting(orderId)
						.flatMap(a -> {
							
							a.setTarget(Constant.TARGET_ROUTING);						
							return auditService.updateOrderNewAudit(getAuditHistoryDtoFromObject(a, null));
							
						})
						.subscribe();	
						
					}
				}
			});			
		
		return Mono.just(new OrderCanonical());
	}

}
