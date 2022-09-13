package com.inretailpharma.digital.deliverymanager.adapter;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RoutingAdapter extends AdapterAbstractUtil implements IRoutingAdapter {
	
	private OrderExternalService routingService;
	private OrderExternalService auditService;
	private ObjectToMapper objectToMapper;
	
	public RoutingAdapter(@Qualifier("routing")OrderExternalService routingService,
			@Qualifier("audit") OrderExternalService auditService,
			ObjectToMapper objectToMapper) {
		this.routingService = routingService;
		this.auditService = auditService;
		this.objectToMapper = objectToMapper;
	}

	@Override
	public Mono<OrderCanonical> createOrder(OrderCanonical orderCanonical) {
		
		Optional.ofNullable(orderCanonical.getStoreCenter())
			.ifPresent(sc -> {
				
				if (sc.isExternalRoutingEnabled()) {
					
					Mono.fromCallable(() -> this.getOrderByEcommerceId(orderCanonical.getEcommerceId()))
					.flatMap(order -> 
						
						routingService.createOrderRouting(orderCanonical.getEcommerceId(),
								objectToMapper.convertIOrderFulfillmentToRoutedOrder(order, orderCanonical.getOrderItems().size()))
						.flatMap(a -> {
							a.setTarget(Constant.TARGET_ROUTING);						
							return auditService.updateOrderNewAudit(getAuditHistoryDtoFromObject(a, null));
							
						})
						
					).subscribe();	
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
					
					routingService.updateOrderRouting(orderId)
					.flatMap(a -> {
						
						a.setTarget(Constant.TARGET_ROUTING);						
						return auditService.updateOrderNewAudit(getAuditHistoryDtoFromObject(a, null));
						
					})
					.subscribe();				
					
				}
			});			
		
		return Mono.just(new OrderCanonical());
	}

}
