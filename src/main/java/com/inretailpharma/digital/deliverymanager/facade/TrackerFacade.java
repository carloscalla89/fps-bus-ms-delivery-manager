package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

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

    public Mono<OrderTrackerResponseCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {    	
    	projectedGroupCanonical.getGroup().stream().forEach(order -> {
    		IOrderFulfillment orderDto = orderTransaction.getOrderByecommerceId(order.getOrderId());    		
    		if (Optional.ofNullable(orderDto).isPresent()) {
    			OrderCanonical orderCanonical = objectToMapper.convertIOrderDtoToOrderFulfillmentCanonical(orderDto);
    			
    			List<IOrderItemFulfillment> orderItemDtoList = orderTransaction.getOrderItemByOrderFulfillmentId(orderDto.getOrderId());
        		List<OrderItemCanonical> orderItemCanonicalList = orderItemDtoList.stream()
        				.map(objectToMapper::convertIOrderItemDtoToOrderItemFulfillmentCanonical)
        				.collect(Collectors.toList());
        		
        		orderCanonical.setMotorizedId(projectedGroupCanonical.getMotorizedId());
        		orderCanonical.setOrderItems(orderItemCanonicalList);
        		
        		OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                orderStatus.setCode(Constant.OrderStatus.ARRIVED.name());
                orderCanonical.setOrderStatus(orderStatus);
        		
        		orderExternalOrderTracker.sendOrderToTracker(orderCanonical);
    		}
    	});
    	
    	OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
    	response.setStatusCode(Constant.OrderTrackerResponseCode.SUCCESS_CODE);
        return Mono.just(response);
    }
}
