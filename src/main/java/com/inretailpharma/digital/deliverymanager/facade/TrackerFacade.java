package com.inretailpharma.digital.deliverymanager.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.FailedOrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.GroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderAssignResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
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
	
	
	public TrackerFacade(OrderTransaction orderTransaction, 
			ObjectToMapper objectToMapper,
			 @Qualifier("orderTracker") OrderExternalService orderExternalOrderTracker,
             @Qualifier("audit") OrderExternalService orderExternalServiceAudit) {
		this.orderTransaction = orderTransaction;
		this.objectToMapper = objectToMapper;
		this.orderExternalOrderTracker = orderExternalOrderTracker;
		this.orderExternalServiceAudit = orderExternalServiceAudit;
	}

    public Mono<OrderAssignResponseCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {   
    	
    	log.info("[START] assign orders to external tracker");
    	
    	List<Long> notMappedOrders = new ArrayList<>();
    	
    	return Flux.fromIterable(projectedGroupCanonical.getGroup())
    		.parallel()
            .runOn(Schedulers.elastic())
	    	.map(group -> {

	    			IOrderFulfillment orderDto = orderTransaction.getOrderByecommerceId(group.getOrderId());    		
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
	                
	                Optional<Long> externalId = Optional.ofNullable(orderCanonical.getExternalId());
	                if (!externalId.isPresent()) {
	                	orderCanonical.setExternalId(group.getInkaDeliveryId());
	                }
	                
	                group.setOrder(orderCanonical);

	                return group;
	    	})
	    	.sequential()
	    	.onErrorContinue((ex, group) -> {
	    		if (group instanceof GroupCanonical) {
	    			Long orderId = ((GroupCanonical)group).getOrderId();
	    			notMappedOrders.add(orderId);
	    			log.error("[ERROR] assign orders to external tracker {} - " , orderId, ex);
	    		} else {
	    			log.error("[ERROR] assign orders to external tracker {} - " , group, ex);
	    		}
	    	})
	    	.collectList()
	    	.map(allGroups -> {	  
	    		log.info("[START] assign orders from group {} to external tracker", projectedGroupCanonical.getGroupName());
	    		log.info("assign orders - mapped orders {}", allGroups.stream().map(GroupCanonical::getOrderId).collect(Collectors.toList()));
	    		log.info("assign orders - not mapped orders {}", notMappedOrders);
	    		
	    		OrderAssignResponseCanonical response = new OrderAssignResponseCanonical();

	    		ProjectedGroupCanonical newProjectedGroupCanonical = new ProjectedGroupCanonical();
	    		newProjectedGroupCanonical.setGroup(allGroups);
	    		newProjectedGroupCanonical.setGroupName(projectedGroupCanonical.getGroupName());
	    		newProjectedGroupCanonical.setMotorizedId(projectedGroupCanonical.getMotorizedId());
	    		newProjectedGroupCanonical.setProjectedEtaReturn(projectedGroupCanonical.getProjectedEtaReturn());
	    		
	    		orderExternalOrderTracker.assignOrders(newProjectedGroupCanonical)
		    		.map(r -> {
		    			log.info("#assign orders from group {} to external tracker - response: {}"
		    					, projectedGroupCanonical.getGroupName(), r);	

		    			if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(r.getAssigmentSuccessful())) {
		    				
		    				List<Long> allFailedOrders = new ArrayList<>();
		    				r.getCreatedOrders().forEach(orderId -> auditOrder(orderId, Constant.OrderStatus.ASSIGNED));
		    				r.getFailedOrders().forEach(order -> {
		    					auditOrder(order.getOrderId(), Constant.OrderStatus.ERROR_ASSIGNED, order.getReason());
		    					allFailedOrders.add(order.getOrderId());
		    				});
		    				notMappedOrders.forEach(orderId -> {
		    					auditOrder(orderId, Constant.OrderStatus.ERROR_ASSIGNED);
		    					allFailedOrders.add(orderId);
		    				});
		    				
		    				response.setStatusCode(
		    					allFailedOrders.isEmpty() ? Constant.OrderTrackerResponseCode.ASSIGN_SUCCESS_CODE 
				    			: Constant.OrderTrackerResponseCode.ASSIGN_PARTIAL_CODE
				    		);
				    		response.setFailedOrders(allFailedOrders);
		    					
		    			} else {
		    				allGroups.stream().forEach(order -> auditOrder(order.getOrderId(), Constant.OrderStatus.ERROR_ASSIGNED));
		    				
		    				response.setStatusCode(Constant.OrderTrackerResponseCode.ASSIGN_ERROR_CODE);
		    				response.setFailedOrders(allGroups.stream().map(GroupCanonical::getOrderId).collect(Collectors.toList())	    						
		    				);
		    			}
		    			return Mono.empty();
		    		}).block();

	    		log.info("[END] assign orders from group {} to external tracker - response {}", projectedGroupCanonical.getGroupName(), response);	
	    		return response;
	    	})
	    	.onErrorResume(ex -> {
	    		log.error("[ERROR] assign orders to external tracker", ex);
	    		projectedGroupCanonical.getGroup().stream().forEach(order -> auditOrder(order.getOrderId(), Constant.OrderStatus.ERROR_ASSIGNED, ex.getMessage()));
	    		OrderAssignResponseCanonical response = new OrderAssignResponseCanonical();
	    		response.setFailedOrders(projectedGroupCanonical.getGroup().stream().map(GroupCanonical::getOrderId).collect(Collectors.toList()));
	    		response.setStatusCode(Constant.OrderTrackerResponseCode.ASSIGN_ERROR_CODE);
	    		return Mono.just(response);
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
