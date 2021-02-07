package com.inretailpharma.digital.deliverymanager.proxy;

import java.util.ArrayList;


import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.AssignedOrdersCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service("orderTracker")
public class OrderTrackerServiceImpl extends AbstractOrderService  implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    public OrderTrackerServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
	public Mono<AssignedOrdersCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {
    	log.info("[START] call to OrderTracker - assignOrders - uri:{} - body:{}",
                externalServicesProperties.getOrderTrackerAssignOrdersUri(), projectedGroupCanonical);
    	
    	return WebClient
            	.create(externalServicesProperties.getOrderTrackerAssignOrdersUri())
            	.post()
            	.bodyValue(projectedGroupCanonical)
            	.retrieve()
            	.bodyToMono(AssignedOrdersCanonical.class)
            	.doOnSuccess(r -> log.info("[END] call to OrderTracker - assignOrders - response {}", r))
	        	.defaultIfEmpty(
	        		 new AssignedOrdersCanonical(new ArrayList<>(), new ArrayList<>(), Constant.OrderTrackerResponseCode.EMPTY_CODE, "EMPTY")
	        	)
	        	.onErrorResume(ex -> {
	        		AssignedOrdersCanonical error = new AssignedOrdersCanonical(new ArrayList<>(), new ArrayList<>()
	        				, Constant.OrderTrackerResponseCode.ERROR_CODE, ex.getMessage());
                    log.error("[ERROR] call to OrderTracker - assignOrders",ex);
                    return Mono.just(error);
                });
	}

	@Override
	public Mono<String> unassignOrders(UnassignedCanonical unassignedCanonical) {
		log.info("[START] call to OrderTracker - unassignOrders - uri:{} - body:{}",
                externalServicesProperties.getOrderTrackerUnassignOrdersUri(), unassignedCanonical);
		
		return WebClient
	        	.create(externalServicesProperties.getOrderTrackerUnassignOrdersUri())
	        	.patch()
	        	.bodyValue(unassignedCanonical)
	        	.exchange()
	        	.flatMap(r -> {
	        		if (r.statusCode().is2xxSuccessful()) {
	        			log.info("[END] call to OrderTracker - unassignOrders - status {}", r.statusCode());
						return r.bodyToMono(Void.class).thenReturn(Constant.OrderTrackerResponseCode.SUCCESS_CODE);
	        		} else {
	        			log.error("[ERROR] call to OrderTracker - unassignOrders - status {}", r.statusCode());
						return r.bodyToMono(Void.class).thenReturn(Constant.OrderTrackerResponseCode.ERROR_CODE);
	        		}
	        	})
	        	.defaultIfEmpty(Constant.OrderTrackerResponseCode.EMPTY_CODE)
	        	.onErrorResume(ex -> {
                    log.error("[ERROR] call to OrderTracker - unassignOrders - ",ex);
                    return Mono.just(Constant.OrderTrackerResponseCode.ERROR_CODE);
                });
	}
	
	@Override
	public Mono<String> updateOrderStatus(Long ecommerceId, String status) {
		log.info("[START] call to OrderTracker - updateOrderStatus - uri:{} - ecommerceId:{} - status:{}",
                externalServicesProperties.getOrderTrackerUpdateOrderStatusUri(), ecommerceId, status);
		
		return WebClient
				.builder()
                .baseUrl(externalServicesProperties.getOrderTrackerUpdateOrderStatusUri())
                .build()
                .patch()
                .uri(builder ->
                		builder
                				.path("/{ecommerceId}/status/{status}")
                                .build(ecommerceId, status))
                .exchange()
	        	.flatMap(r -> {
	        		if (r.statusCode().is2xxSuccessful()) {

	        			log.info("[END] call to OrderTracker - updateOrderStatus - status {}", r.statusCode());

	        			return r.bodyToMono(Void.class).thenReturn(Constant.OrderTrackerResponseCode.SUCCESS_CODE);

	        		} else {
	        			log.error("[ERROR] call to OrderTracker - updateOrderStatus - status {}", r.statusCode());

	        			return r.bodyToMono(Void.class).thenReturn(Constant.OrderTrackerResponseCode.ERROR_CODE);
	        		}
	        	})
	        	.defaultIfEmpty(Constant.OrderTrackerResponseCode.EMPTY_CODE)
	        	.onErrorResume(ex -> {
                    log.error("[ERROR] call to OrderTracker - updateOrderStatus - ",ex);
                    return Mono.just(Constant.OrderTrackerResponseCode.ERROR_CODE);
                });
	}


	@Override
	public Mono<OrderCanonical> updateOrderStatus(Long ecommerceId, ActionDto actionDto) {
		log.info("[START] call to OrderTracker - updateOrderStatus - uri:{} - ecommerceId:{} - action:{}",
				externalServicesProperties.getOrderTrackerUpdateOrderStatusUri(), ecommerceId, actionDto.getAction());

		log.info("[START] connect order-tracker   - ecommerceId:{} - actionOrder:{}",
				ecommerceId, actionDto.getAction());

		Constant.OrderStatusTracker orderStatusInkatracker = Constant.OrderStatusTracker.getByActionName(actionDto.getAction());

		log.info("url to create orderTracker:{}",externalServicesProperties.getOrderTrackerCreateOrderUri());

		return WebClient
				.builder()
				.baseUrl(externalServicesProperties.getOrderTrackerUpdateOrderStatusUri())
				.build()
				.patch()
				.uri(builder ->
						builder
								.path("/{ecommerceId}/status/{status}")
								.build(ecommerceId, orderStatusInkatracker.getTrackerLiteStatus()))
				.exchange()
				.flatMap(clientResponse -> mapResponseFromUpdateTracker(clientResponse, ecommerceId, orderStatusInkatracker))
				.doOnSuccess(s -> log.info("Response is Success in order-tracker:{}",s))
				.defaultIfEmpty(
						new OrderCanonical(
								ecommerceId,
								Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.getCode(),
								Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.name())
				)
				.doOnError(e -> {
					e.printStackTrace();
					log.error("Error in inkatracker-lite when its sent to update:{}",e.getMessage());
				})
				.onErrorResume(e -> mapResponseErrorFromTracker(e, ecommerceId,
						ecommerceId, orderStatusInkatracker.getOrderStatusError().name(),
						actionDto.getOrderCancelCode(), actionDto.getOrderCancelObservation())
				);
	}

	@Override
	public Mono<OrderCanonical> sendOrderToOrderTracker(OrderCanonical orderCanonical) {
		log.info("[START] call to OrderTracker - sendOrderToTracker - uri:{} - body:{}",
				externalServicesProperties.getOrderTrackerCreateOrderUri(), orderCanonical);

		return WebClient
				.create(externalServicesProperties.getOrderTrackerCreateOrderUri())
				.post()
				.bodyValue(orderCanonical)
				.retrieve()
				.bodyToMono(String.class)
				.map(body -> {
					log.info("[END] call to OrderTracker - sendOrderToTracker - s:{}", body);
					return orderCanonical;
				})
				.defaultIfEmpty(
						new OrderCanonical()
				)
				.onErrorResume(ex -> {
					log.error("[ERROR] call to OrderTracker - sendOrderToTracker", ex);
					return Mono.just(new OrderCanonical());
				});
	}
	@Override
	public Mono<OrderCanonical> sendOrderToOrderTracker(OrderCanonical orderCanonical, ActionDto actionDto) {

		log.info("[START] connect order-tracker   - ecommerceId:{} - actionOrder:{}",
				orderCanonical.getEcommerceId(), actionDto.getAction());

		Constant.OrderStatusTracker orderStatusInkatracker = Constant.OrderStatusTracker.getByActionName(actionDto.getAction());

		log.info("url to create orderTracker:{}",externalServicesProperties.getOrderTrackerCreateOrderUri());


		return WebClient
				.create(externalServicesProperties.getOrderTrackerCreateOrderUri())
				.post()
				.bodyValue(orderCanonical)
				.exchange()
				.flatMap(clientResponse -> mapResponseFromUpdateTracker(clientResponse, orderCanonical.getEcommerceId(), orderStatusInkatracker))
				.doOnSuccess(s -> log.info("Response is Success in inkatracker-lite Update:{}",s))
				.defaultIfEmpty(
						new OrderCanonical(
								orderCanonical.getEcommerceId(),
								Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.getCode(),
								Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.name())
				)
				.doOnError(e -> {
					e.printStackTrace();
					log.error("Error in inkatracker-lite when its sent to update:{}",e.getMessage());
				})
				.onErrorResume(e -> mapResponseErrorFromTracker(e, orderCanonical.getEcommerceId(),
						orderCanonical.getEcommerceId(), orderStatusInkatracker.getOrderStatusError().name(),
						actionDto.getOrderCancelCode(), actionDto.getOrderCancelObservation())
				);
	}
}
