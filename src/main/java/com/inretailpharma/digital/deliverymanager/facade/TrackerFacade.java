package com.inretailpharma.digital.deliverymanager.facade;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class TrackerFacade {
	
	private OrderTransaction orderTransaction;
	private ObjectToMapper objectToMapper;
	private OrderExternalService orderExternalOrderTracker;
	private OrderExternalService orderExternalServiceAudit;
	private CenterCompanyService centerCompanyService;
	
	
	public TrackerFacade(OrderTransaction orderTransaction, 
			ObjectToMapper objectToMapper,
			 @Qualifier("orderTracker") OrderExternalService orderExternalOrderTracker,
             @Qualifier("audit") OrderExternalService orderExternalServiceAudit,
             CenterCompanyService centerCompanyService) {
		this.orderTransaction = orderTransaction;
		this.objectToMapper = objectToMapper;
		this.orderExternalOrderTracker = orderExternalOrderTracker;
		this.orderExternalServiceAudit = orderExternalServiceAudit;
		this.centerCompanyService = centerCompanyService;
	}

    public Mono<OrderTrackerResponseCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {   
    	
    	log.info("[START] assign orders to external tracker");
    	
    	return Flux.fromIterable(projectedGroupCanonical.getGroup())
    		.parallel()
            .runOn(Schedulers.elastic())
	    	.map(group -> {
	    		Long orderId = group.getOrderId();
	    		log.info("[START] send order {} to external tracker", orderId);
	    		
	    		IOrderFulfillment orderDto = orderTransaction.getOrderByecommerceId(orderId);    		
	    		OrderCanonical orderCanonical = objectToMapper.convertIOrderDtoToOrderFulfillmentCanonical(orderDto);
    			
    			List<IOrderItemFulfillment> orderItemDtoList = orderTransaction.getOrderItemByOrderFulfillmentId(orderDto.getOrderId());
        		List<OrderItemCanonical> orderItemCanonicalList = orderItemDtoList.stream()
        				.map(objectToMapper::convertIOrderItemDtoToOrderItemFulfillmentCanonical)
        				.collect(Collectors.toList());

        		orderCanonical.setOrderItems(orderItemCanonicalList);
                
                Optional.ofNullable(group.getPickUpDetails()).ifPresent(pickUpDetails -> {
                	orderCanonical.setShelfList(pickUpDetails.getShelfList());
                	orderCanonical.setPayBackEnvelope(pickUpDetails.getPayBackEnvelope());
                });                
                
                group.setCreationStatus(Constant.OrderTrackerResponseCode.SUCCESS_CODE);
                orderExternalOrderTracker.sendOrderToTracker(orderCanonical)
                .onErrorResume(ex -> {
                	log.error("[ERROR] send order {} to external tracker", orderId, ex);  
                	group.setCreationStatus(Constant.OrderTrackerResponseCode.ERROR_CODE);
                	
                	return Mono.empty();
                }).block();
                log.info("[END] send order {} to external tracker", orderId);                
                
	    		return group;	    		
	    	})
	    	.sequential()
	    	.collectList()
	    	.map(group -> {	  
	    		
	    		ProjectedGroupCanonical newProjectedGroupCanonical = new ProjectedGroupCanonical();
	    		newProjectedGroupCanonical.setGroup(group);
	    		newProjectedGroupCanonical.setGroupName(projectedGroupCanonical.getGroupName());
	    		newProjectedGroupCanonical.setMotorizedId(projectedGroupCanonical.getMotorizedId());
	    		newProjectedGroupCanonical.setProjectedEtaReturn(projectedGroupCanonical.getProjectedEtaReturn());
	    		
	    		orderExternalOrderTracker.assignOrders(projectedGroupCanonical)
	    			.map(statusCode -> {
	    				
	    				if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(statusCode)) {
	    					group.forEach(successGroup -> {
	    						if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(successGroup.getCreationStatus())) {
	    							orderExternalServiceAudit.updateOrderReactive(
	    	                        		new OrderCanonical(successGroup.getOrderId(),
	    	                        				Constant.OrderStatus.ASSIGNED.getCode(),
	    	                        				Constant.OrderStatus.ASSIGNED.name())).subscribe();
	    						} else {
	    							orderExternalServiceAudit.updateOrderReactive(
	    	                        		new OrderCanonical(successGroup.getOrderId(),
	    	                        				Constant.OrderStatus.ERROR_ASSIGNED.getCode(),
	    	                        				Constant.OrderStatus.ERROR_ASSIGNED.name())).subscribe();
	    						}
	        				});	 
	    				} else {	    					
	    					group.forEach(errorGroup -> {	        					
	    						orderExternalServiceAudit.updateOrderReactive(
    	                        		new OrderCanonical(errorGroup.getOrderId(),
    	                        				Constant.OrderStatus.ERROR_ASSIGNED.getCode(),
    	                        				Constant.OrderStatus.ERROR_ASSIGNED.name())).subscribe();
	        				});	    					
	    				}

	    				return Mono.empty();
	    			}).block();
	    		
	    		OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
	        	response.setStatusCode(Constant.OrderTrackerResponseCode.SUCCESS_CODE);
	            return response;
	    	});
    }
    
    public Mono<OrderTrackerResponseCanonical> unassignOrders(UnassignedCanonical unassignedCanonical) {

    	return orderExternalOrderTracker.unassignOrders(unassignedCanonical)
    			.map(statusCode -> {
    				
    				unassignedCanonical.getOrders().forEach(orderId -> {
    					
    					if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(statusCode)) {
	    					orderExternalServiceAudit.updateOrderReactive(
	                        		new OrderCanonical(orderId,
	                        				Constant.OrderStatus.PREPARED.getCode(),
	                        				Constant.OrderStatus.PREPARED.name())).subscribe();
	    				} else {
	    					orderExternalServiceAudit.updateOrderReactive(
	                        		new OrderCanonical(orderId,
	                        				Constant.OrderStatus.ERROR_PREPARED .getCode(),
	                        				Constant.OrderStatus.ERROR_PREPARED.name())).subscribe();
	    				}
    				});
    				
    				OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
    	        	response.setStatusCode(statusCode);
    	        	return Mono.just(response);
    			}).block();
    }
    
    public Mono<OrderTrackerResponseCanonical> updateOrderStatus(Long ecommerceId, String status) {    	
    	return orderExternalOrderTracker.updateOrderStatus(ecommerceId, status)
    		.map(statusCode -> {
    			
    			if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(statusCode)) {
					orderExternalServiceAudit.updateOrderReactive(
                    		new OrderCanonical(ecommerceId,
                    				Constant.OrderStatus.CANCELLED_ORDER.getCode(),
                    				Constant.OrderStatus.CANCELLED_ORDER.name())).subscribe();
				} else {
					orderExternalServiceAudit.updateOrderReactive(
                    		new OrderCanonical(ecommerceId,
                    				Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.getCode(),
                    				Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.name())).subscribe();
				}
    			
    			OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
	        	response.setStatusCode(statusCode);
	        	return Mono.just(response);
    			
    		}).block();
    }
}
