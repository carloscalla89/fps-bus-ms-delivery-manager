package com.inretailpharma.digital.deliverymanager.facade;

import java.util.*;
import java.util.stream.Collectors;

import com.inretailpharma.digital.deliverymanager.adapter.AdapterInterface;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderSynchronizeDto;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.proxy.OrderFacadeProxy;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;

import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.GroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderAssignResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderToAssignCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
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
	private OrderFacadeProxy orderFacadeProxy;
	private AdapterInterface adapterInterface;

	@Autowired
	public TrackerFacade(OrderTransaction orderTransaction,
						 ObjectToMapper objectToMapper,
						 @Qualifier("orderTracker") OrderExternalService orderExternalOrderTracker,
						 @Qualifier("audit") OrderExternalService orderExternalServiceAudit,
						 @Qualifier("trackeradapter") AdapterInterface adapterInterface,
						 OrderFacadeProxy orderFacadeProxy) {
		this.orderTransaction = orderTransaction;
		this.objectToMapper = objectToMapper;
		this.orderExternalOrderTracker = orderExternalOrderTracker;
		this.orderExternalServiceAudit = orderExternalServiceAudit;
		this.adapterInterface = adapterInterface;
		this.orderFacadeProxy = orderFacadeProxy;
	}

    public Mono<OrderAssignResponseCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {   
    	
    	log.info("[START] assign orders to external tracker");
    	
    	List<Long> notMappedOrders = new ArrayList<>();
    	
    	return Flux.fromIterable(projectedGroupCanonical.getGroup())
    		.parallel()
            .runOn(Schedulers.elastic())
	    	.map(group -> {
  		
		    		OrderCanonical orderCanonical = getOrder(group.getOrderId());
	                
	                Optional.ofNullable(group.getPickUpDetails()).ifPresent(pickUpDetails -> {
	                	orderCanonical.setShelfList(pickUpDetails.getShelfList());
	                	orderCanonical.setPayBackEnvelope(pickUpDetails.getPayBackEnvelope());
	                }); 
	                
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
	    	.flatMap(allGroups -> {
	    		log.info("[START] assign orders from group {} to external tracker", projectedGroupCanonical.getGroupName());
	    		log.info("assign orders - mapped orders {}", allGroups.stream().map(GroupCanonical::getOrderId).collect(Collectors.toList()));
	    		log.info("assign orders - not mapped orders {}", notMappedOrders);
	    		
	    		OrderAssignResponseCanonical response = new OrderAssignResponseCanonical();

	    		ProjectedGroupCanonical newProjectedGroupCanonical = new ProjectedGroupCanonical();
	    		newProjectedGroupCanonical.setGroup(allGroups);
	    		newProjectedGroupCanonical.setGroupName(projectedGroupCanonical.getGroupName());
	    		newProjectedGroupCanonical.setMotorizedId(projectedGroupCanonical.getMotorizedId());
	    		newProjectedGroupCanonical.setProjectedEtaReturn(projectedGroupCanonical.getProjectedEtaReturn());
	    		
	    		return orderExternalOrderTracker
							.assignOrders(newProjectedGroupCanonical)
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
								return response;
							});

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
		return orderExternalOrderTracker
				.unassignOrders(unassignedCanonical)
				.flatMap(statusCode -> {
					log.info("#unassing orders from group {} - external tracker - statusCode: {}"
							, unassignedCanonical.getGroupName(), statusCode);
					unassignedCanonical.getOrders().forEach(orderId -> {

						if (Constant.OrderTrackerResponseCode.SUCCESS_CODE.equals(statusCode)) {
							auditOrder(orderId, Constant.OrderStatus.PREPARED_ORDER);
						} else {
							auditOrder(orderId, Constant.OrderStatus.ERROR_PREPARED);
						}
					});

					OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
					response.setStatusCode(statusCode);
					return Mono.just(response);
				});
	}

	public Mono<OrderTrackerResponseCanonical> updateOrderStatus(Long ecommerceId, String status) {
		log.info("[START] update order: {} status: {} - external tracker", ecommerceId, status);

		return Mono
				.justOrEmpty(orderTransaction.getOrderLightByecommerceId(ecommerceId))
				.flatMap(filtredOrder -> {

					if (!filtredOrder.getServiceType().equalsIgnoreCase(Constant.ServiceTypeCodes.PICKUP)) {
						return orderExternalOrderTracker
								.updateOrderStatus(filtredOrder.getEcommerceId(), status)
								.filter(resultCode -> resultCode.equalsIgnoreCase(Constant.OrderTrackerResponseCode.SUCCESS_CODE))
								.flatMap(resultCode -> Mono.just(getOrderTrackerResponse(
										resultCode, filtredOrder.getOrderId(), filtredOrder.getEcommerceId(), status, filtredOrder.getPaymentType()))
								).switchIfEmpty(Mono.defer(() -> Mono.just(getOrderTrackerResponse(
										Constant.OrderTrackerResponseCode.ERROR_CODE, filtredOrder.getOrderId(),
										filtredOrder.getEcommerceId(), status, filtredOrder.getPaymentType())))
								);
					}

					return Mono.just(getOrderTrackerResponse(
							Constant.OrderTrackerResponseCode.SUCCESS_CODE, filtredOrder.getOrderId(), filtredOrder.getEcommerceId(), status, filtredOrder.getPaymentType())
					);


				}).switchIfEmpty(Mono.defer(Mono::empty) );

	}

	public Mono<OrderTrackerResponseCanonical> sendOrder(OrderToAssignCanonical orderToAssignCanonical) {
		log.info("[START] sendOrder - external tracker");

		OrderCanonical orderCanonical = this.getOrder(orderToAssignCanonical.getOrderId());
		orderCanonical.setOrderStatus(null);

		return orderExternalOrderTracker
				.sendOrderToOrderTracker(orderCanonical)
				.flatMap(resp -> {
					OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
					response.setStatusCode(Constant.OrderTrackerResponseCode.SUCCESS_CODE);
					return Mono.just(response);
				});

	}

	public Flux<OrderTrackerResponseCanonical>  synchronizeOrderStatus(List<OrderSynchronizeDto> ordersList) {
		log.info("[START] synchronizeOrderStatus list:{}",ordersList);

		List<IOrderFulfillment> iOrdersFulfillment = orderTransaction
														.getOrderLightByecommercesIds(
																ordersList
																		.stream()
																		.map(OrderSynchronizeDto::getEcommerceId)
																		.collect(Collectors.toSet())
														);


		return Flux
				.fromIterable(iOrdersFulfillment)
				.flatMap(order -> {
					OrderSynchronizeDto orderSynchronizeDto = ordersList
																.stream()
																.filter(o -> o.getEcommerceId().equals(order.getEcommerceId()))
																.findFirst()
																.get();

					return Flux
							.fromIterable(orderSynchronizeDto.getHistory())
							.sort(Comparator. comparing(obj -> DateUtils.getLocalDateTimeFromStringWithFormat(obj.getActionDate())))
							.flatMap(oh -> {

								ActionDto actionDto = new ActionDto();
								actionDto.setAction(oh.getAction());
								actionDto.setOrigin(orderSynchronizeDto.getOrigin());
								actionDto.setOrderCancelCode(oh.getOrderCancelCode());
								actionDto.setOrderCancelObservation(oh.getOrderCancelObservation());
								actionDto.setMotorizedId(oh.getMotorizedId());
								actionDto.setUpdatedBy(oh.getUpdatedBy());

								return orderFacadeProxy.sendToUpdateOrder(
										order.getOrderId(),
										order.getEcommerceId(),
										order.getExternalId(),
										actionDto,
										order.getServiceType(),
										order.getServiceTypeShortCode(),
										order.getClassImplement(),
										order.getSource(),
										order.getServiceChannel(),
										order.getCompanyCode(),
										order.getCenterCode(),
										order.getStatusCode(),
										order.getFirstName(),
										order.getPhone(),
										order.getScheduledTime(),
										order.getSendNewFlow(),
										order.getSendNotificationByChannel()
								);

							})
							.flatMap(response -> {
								OrderTrackerResponseCanonical orderTrackerResponseCanonical = new OrderTrackerResponseCanonical();
								orderTrackerResponseCanonical.setEcommerceId(response.getEcommerceId());
								orderTrackerResponseCanonical.setStatusCode(response.getOrderStatus().getCode());
								orderTrackerResponseCanonical.setStatusDescription(response.getOrderStatus().getName());
								orderTrackerResponseCanonical.setStatusDetail(response.getOrderStatus().getDetail());

								return Flux.just(orderTrackerResponseCanonical);
							});







				});

	}

	private OrderCanonical getOrder(Long orderId) {
		IOrderFulfillment orderDto = orderTransaction.getOrderByecommerceId(orderId);
		OrderCanonical orderCanonical = objectToMapper.convertIOrderDtoToOrderFulfillmentCanonical(orderDto);

		List<IOrderItemFulfillment> orderItemDtoList = orderTransaction.getOrderItemByOrderFulfillmentId(orderDto.getOrderId());
		List<OrderItemCanonical> orderItemCanonicalList = orderItemDtoList.stream()
				.map(o -> objectToMapper.convertIOrderItemDtoToOrderItemFulfillmentCanonical(o, orderDto.getPartial()))
				.collect(Collectors.toList());		
		
		orderCanonical.setOrderItems(orderItemCanonicalList);
		
		return orderCanonical;
    }
    
    private void auditOrder(Long ecommerceId, Constant.OrderStatus status) {
    	orderExternalServiceAudit.updateOrderReactive(
        		new OrderCanonical(ecommerceId, status.getCode(), status.name(), null)).subscribe();
    }
    
    private void auditOrder(Long ecommerceId, Constant.OrderStatus status, String detail) {
    	orderExternalServiceAudit.updateOrderReactive(
        		new OrderCanonical(ecommerceId, status.getCode(), status.name(), detail)).subscribe();
    }

    private OrderTrackerResponseCanonical getOrderTrackerResponse(String result,Long orderId, Long ecommerceId, String trackerStatus,
																  String paymentType) {

		log.info("#update order: {} status: {} - external tracker - statusCode: {}", ecommerceId, trackerStatus, result);

		Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getOrderStatusByTrackerStatus(trackerStatus, paymentType);

		// Para actualizar los últimos status en el fulfillment
		orderTransaction.updateStatusOrder(orderId, orderStatus.getCode(), null);

		// enviar a la auditoría el cambio del status
		auditOrder(ecommerceId, orderStatus);

		OrderTrackerResponseCanonical response = new OrderTrackerResponseCanonical();
		response.setStatusCode(result);
		return response;

	}

	public Mono<OrderCanonical> getOrderByEcommerceId(Long ecommerceId) {

		IOrderFulfillment iOrderFulfillmentLight = orderTransaction.getOrderLightByecommerceId(ecommerceId);

		return Optional
				.ofNullable(iOrderFulfillmentLight)
				.map(order -> adapterInterface.getOrder(order))
				.orElse(Mono.empty());

	}

	public Flux<OrderCanonical> getOrderByEcommerceIds(String ecommerceIds) {
		log.info("getOrderByEcommerceIds:{}",ecommerceIds);

		return Flux
				.fromIterable(orderTransaction
						.getOrderLightByecommercesIds(
								Arrays.stream(ecommerceIds.split(",")).map(Long::parseLong).collect(Collectors.toSet())
						))
				.parallel()
				.runOn(Schedulers.elastic())
				.flatMap(orders -> {
					log.info("in orders flux");
					return adapterInterface.getOrder(orders).flux();
				}).ordered((o1,o2) -> o2.getEcommerceId().intValue() - o1.getEcommerceId().intValue());

	}
}
