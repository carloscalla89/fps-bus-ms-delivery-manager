package com.inretailpharma.digital.deliverymanager.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.GroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderAssignResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.Constant.OrderTrackerStatusMapper;

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

    public Mono<OrderAssignResponseCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {   
    	
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
                	group.setCreationLog(ex.getMessage());
                	return Mono.empty();
                }).block();
                log.info("[END] send order {} to external tracker", orderId);                
                
	    		return group;	    		
	    	})
	    	.sequential()
	    	.collectList()
	    	.map(allOrders -> {	  
	    		log.info("[START] assign orders from group {} to external tracker", projectedGroupCanonical.getGroupName());
	    		
	    		OrderAssignResponseCanonical response = new OrderAssignResponseCanonical();
	    		
	    		List<Long> failedOrdersIds = allOrders.stream()
	    				.filter(f -> !Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(f.getCreationStatus()))
	    				.map(o -> o.getOrderId())
	    				.collect(Collectors.toList());
	    		
	    		List<GroupCanonical> okOrders = allOrders.stream()
	    				.filter(f -> Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(f.getCreationStatus()))
	    				.collect(Collectors.toList());
	    		
	    		log.info("#{} orders sent to the external tracker - ok: {} - failed {}",
	    				allOrders.size(), okOrders.size(), failedOrdersIds.size());

	    		ProjectedGroupCanonical newProjectedGroupCanonical = new ProjectedGroupCanonical();
	    		newProjectedGroupCanonical.setGroup(okOrders);
	    		newProjectedGroupCanonical.setGroupName(projectedGroupCanonical.getGroupName());
	    		newProjectedGroupCanonical.setMotorizedId(projectedGroupCanonical.getMotorizedId());
	    		newProjectedGroupCanonical.setProjectedEtaReturn(projectedGroupCanonical.getProjectedEtaReturn());
	    		
	    		if (!okOrders.isEmpty()) {
	    			orderExternalOrderTracker.assignOrders(newProjectedGroupCanonical)
		    		.map(statusCode -> {
		    			log.info("#assign orders from group {} to external tracker - statusCode: {}"
		    					, projectedGroupCanonical.getGroupName(), statusCode);	 
		    			
		    			if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(statusCode)) {
		    				allOrders.stream().forEach(o -> {
		    					if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(o.getCreationStatus())) {
		    						auditOrder(o.getOrderId(), Constant.OrderStatus.ASSIGNED);
		    					} else {
		    						auditOrder(o.getOrderId(), Constant.OrderStatus.ERROR_ASSIGNED);
		    					}
		    				});
		    					
		    				response.setStatusCode(
				    			failedOrdersIds.isEmpty() ? Constant.OrderTrackerResponseCode.ASSIGN_SUCCESS_CODE 
				    			: Constant.OrderTrackerResponseCode.ASSIGN_PARTIAL_CODE
				    		);
				    		response.setFailedOrders(failedOrdersIds);
		    					
		    			} else {
		    				allOrders.stream().forEach(o -> auditOrder(o.getOrderId(), Constant.OrderStatus.ERROR_ASSIGNED, o.getCreationLog()));
		    				
		    				response.setStatusCode(Constant.OrderTrackerResponseCode.ASSIGN_ERROR_CODE);
		    				response.setFailedOrders(allOrders.stream()
		    						.map(o -> o.getOrderId())
		    	    				.collect(Collectors.toList())	    						
		    				);
		    			}
		    			return Mono.empty();
		    		}).block();
	    		} else {
	    			allOrders.stream().forEach(o -> auditOrder(o.getOrderId(), Constant.OrderStatus.ERROR_ASSIGNED));
	    			
	    			response.setStatusCode(Constant.OrderTrackerResponseCode.ASSIGN_ERROR_CODE);
    				response.setFailedOrders(allOrders.stream()
    						.map(o -> o.getOrderId())
    	    				.collect(Collectors.toList())	    						
    				);
	    		}
	    		
	    		log.info("[END] assign orders from group {} to external tracker", projectedGroupCanonical.getGroupName());	    		
	            return response;
	    	});
    }
    
    public Mono<OrderTrackerResponseCanonical> unassignOrders(UnassignedCanonical unassignedCanonical) {
    	log.info("[START] unassing orders from group {} - external tracker", unassignedCanonical.getGroupName());
    	return orderExternalOrderTracker.unassignOrders(unassignedCanonical)
    			.map(statusCode -> {
    				log.info("#unassing orders from group {} - external tracker - statusCode: {}"
    						, unassignedCanonical.getGroupName(), statusCode);	
    				unassignedCanonical.getOrders().forEach(orderId -> {
    					
    					if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(statusCode)) {
    						auditOrder(orderId, Constant.OrderStatus.PREPARED);
	    				} else {
	    					auditOrder(orderId, Constant.OrderStatus.ERROR_PREPARED);
	    				}
    				});
    				
    				OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
    	        	response.setStatusCode(statusCode);
    	        	return Mono.just(response);
    			}).block();
    }
    
    public Mono<OrderTrackerResponseCanonical> updateOrderStatus(Long ecommerceId, String status) {    
    	log.info("[START] update order: {} status: {} - external tracker", ecommerceId, status);
    	return orderExternalOrderTracker.updateOrderStatus(ecommerceId, status)
    		.map(statusCode -> {
    			log.info("#update order: {} status: {} - external tracker - statusCode: {}", ecommerceId, status, statusCode);
    			
    			OrderTrackerStatusMapper statusMapper =  OrderTrackerStatusMapper.getByName(status);
    			if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(statusCode)) {
    				auditOrder(ecommerceId, statusMapper.getSuccessStatus());
				} else {
					auditOrder(ecommerceId, statusMapper.getErrorStatus());
				}
    			
    			OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
	        	response.setStatusCode(statusCode);
	        	return Mono.just(response);
    			
    		}).block();
    }
    
    private void auditOrder(Long ecommerceId, Constant.OrderStatus status) {
    	orderExternalServiceAudit.updateOrderReactive(
        		new OrderCanonical(ecommerceId, status.getCode(), status.name(), null)).subscribe();
    }
    
    private void auditOrder(Long ecommerceId, Constant.OrderStatus status, String detail) {
    	orderExternalServiceAudit.updateOrderReactive(
        		new OrderCanonical(ecommerceId, status.getCode(), status.name(), detail)).subscribe();
    }
}
