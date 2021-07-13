package com.inretailpharma.digital.deliverymanager.facade;

import java.util.*;
import java.util.stream.Collectors;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ITrackerAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.OrderTrackerAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderSynchronizeDto;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.strategy.UpdateTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;


import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.GroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderAssignResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class TrackerFacade extends FacadeAbstractUtil{

	private OrderCancellationService orderCancellationService;

	private ITrackerAdapter iOrderTrackerAdapter;
	private IAuditAdapter iAuditAdapter;

	private UpdateTracker updateTracker;

	@Autowired
	public TrackerFacade(@Qualifier("orderTrackerAdapter") ITrackerAdapter iOrderTrackerAdapter,
						 @Qualifier("auditAdapter") IAuditAdapter iAuditAdapter,
						 OrderCancellationService orderCancellationService,
						 @Qualifier("updateTracker") UpdateTracker updateTracker) {
		this.iOrderTrackerAdapter = iOrderTrackerAdapter;
		this.iAuditAdapter = iAuditAdapter;
		this.orderCancellationService = orderCancellationService;
		this.updateTracker = updateTracker;
	}

    public Mono<OrderAssignResponseCanonical> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {   
    	
    	log.info("[START] assign orders to external tracker");

    	return getOrdersByEcommerceIds(
    				projectedGroupCanonical
							.getGroup()
							.stream()
							.map(GroupCanonical::getOrderId)
							.collect(Collectors.toSet())
				)
				.flatMap(order -> {

					GroupCanonical group =  projectedGroupCanonical
												.getGroup()
												.stream()
												.filter(r -> r.getOrderId().equals(order.getEcommerceId()))
												.findFirst()
												.get();

					Optional.ofNullable(group.getPickUpDetails()).ifPresent(pickUpDetails -> {
						order.setShelfList(pickUpDetails.getShelfList());
						order.setPayBackEnvelope(pickUpDetails.getPayBackEnvelope());
					});

					group.setOrder(order);

					return Flux.just(group);

				})
				.buffer()
				.flatMap(allGroups -> {

					ProjectedGroupCanonical newProjectedGroupCanonical = new ProjectedGroupCanonical();
					newProjectedGroupCanonical.setGroup(allGroups);
					newProjectedGroupCanonical.setGroupName(projectedGroupCanonical.getGroupName());
					newProjectedGroupCanonical.setMotorizedId(projectedGroupCanonical.getMotorizedId());
					newProjectedGroupCanonical.setProjectedEtaReturn(projectedGroupCanonical.getProjectedEtaReturn());
					newProjectedGroupCanonical.setUpdateBy(projectedGroupCanonical.getUpdateBy());
					newProjectedGroupCanonical.setSource(Constant.UPDATED_BY_INKATRACKER_WEB);

					return ((OrderTrackerAdapter)iOrderTrackerAdapter)
								.assignOrders(newProjectedGroupCanonical, allGroups);


				})
				.flatMap(result ->
						updateOrderInfulfillment(
								result, result.getEcommerceId(), Constant.UPDATED_BY_INKATRACKER_WEB, Constant.TARGET_TRACKER,
								projectedGroupCanonical.getUpdateBy(), null)
				)
				.flatMap(result -> iAuditAdapter.updateAudit(result, projectedGroupCanonical.getUpdateBy()))
                .onErrorContinue((e,o) -> {
                    e.printStackTrace();
                    log.error("Error during update status order assigning - error:{}, object:{}",e.getMessage(), o);

                    Long ecommerceId;

                    if (o instanceof OrderCanonical) {
                        ecommerceId = ((OrderCanonical)o).getEcommerceId();
                    } else {
                        ecommerceId = 0L;
                    }

                    processErrorOrdersToSendAudit(
                            "Error during update status order assigning " + o + " - error: " + e.getMessage() +
                                    " - groupname:" + projectedGroupCanonical.getGroupName(),
                            Collections.singletonList(ecommerceId), projectedGroupCanonical.getUpdateBy()
                    );

                })
				.buffer()
				.flatMap(resultFinal -> {
					log.info("The processs of assigned is success");

					OrderAssignResponseCanonical response = new OrderAssignResponseCanonical();
					response.setStatusCode(Constant.OrderTrackerResponseCode.ASSIGN_SUCCESS_CODE);

					return Mono.just(response);
				})
				.switchIfEmpty(Flux.defer(() -> {
					log.error("The process of assigned is empty");

					OrderAssignResponseCanonical response = new OrderAssignResponseCanonical();
					response.setStatusCode(Constant.OrderTrackerResponseCode.ASSIGN_ERROR_CODE);
					response.setDetail("The process of assigned is empty");
					response.setFailedOrders(
							projectedGroupCanonical
									.getGroup().stream().map(GroupCanonical::getOrderId)
									.collect(Collectors.toList())
					);

                    processErrorOrdersToSendAudit(
                            "The process of assigned is empty" +
                                    " - groupname:" + projectedGroupCanonical.getGroupName(),
                            projectedGroupCanonical
                                    .getGroup().stream().map(GroupCanonical::getOrderId)
                                    .collect(Collectors.toList()), projectedGroupCanonical.getUpdateBy()
                    );

					return Mono.just(response);

				}))
				.onErrorResume(e -> {
					e.printStackTrace();
					log.error("The process of assigned of orders is failed");

					OrderAssignResponseCanonical response = new OrderAssignResponseCanonical();
					response.setStatusCode(Constant.OrderTrackerResponseCode.ASSIGN_ERROR_CODE);
					response.setDetail("The process of assigned has failed - detail:"+e.getMessage());
					response.setFailedOrders(
							projectedGroupCanonical
									.getGroup().stream().map(GroupCanonical::getOrderId)
									.collect(Collectors.toList())
					);

                    processErrorOrdersToSendAudit(
                            "The process of assigned of orders is failed - error  " + e.getMessage() +
                                    " - groupname:" + projectedGroupCanonical.getGroupName(),
                            projectedGroupCanonical
                                    .getGroup().stream().map(GroupCanonical::getOrderId)
                                    .collect(Collectors.toList()), projectedGroupCanonical.getUpdateBy()
                    );

					return Mono.just(response);
				})
				.single();
	}

	public Mono<OrderTrackerResponseCanonical> unassignOrders(UnassignedCanonical unassignedCanonical) {
		log.info("[START] unassing orders from group {} - external tracker", unassignedCanonical.getGroupName());
		unassignedCanonical.setSource(Constant.UPDATED_BY_INKATRACKER_WEB);

		return ((OrderTrackerAdapter)iOrderTrackerAdapter)
					.unassignOrders(unassignedCanonical)
					.flatMap(result ->
							updateOrderInfulfillment(
									result, result.getEcommerceId(), Constant.UPDATED_BY_INKATRACKER_WEB, Constant.TARGET_TRACKER,
									unassignedCanonical.getUpdateBy(), null)
					)
					.flatMap(result -> iAuditAdapter.updateAudit(result, unassignedCanonical.getUpdateBy()))
					.buffer()
					.flatMap(resultFinal -> {
						log.info("The processs of unassigned is success:{}",resultFinal);

						OrderTrackerResponseCanonical orderTracker = new OrderTrackerResponseCanonical();
						orderTracker.setStatusCode(Constant.OrderTrackerResponseCode.SUCCESS_CODE);

						return Mono.just(orderTracker);
					})
					.switchIfEmpty(Flux.defer(() -> {
						log.error("The processs of unassigned is empty");

						OrderTrackerResponseCanonical orderTracker = new OrderTrackerResponseCanonical();
						orderTracker.setStatusCode(Constant.OrderTrackerResponseCode.ERROR_CODE);
						orderTracker.setStatusDetail("The processs is empty");
						return Mono.just(orderTracker);

					}))
					.onErrorResume(e -> {
						e.printStackTrace();
						log.error("The processs of unassigned is error");

						OrderTrackerResponseCanonical orderTracker = new OrderTrackerResponseCanonical();
						orderTracker.setStatusCode(Constant.OrderTrackerResponseCode.ERROR_CODE);
						orderTracker.setStatusDetail(e.getMessage());

						return Mono.just(orderTracker);
					})
					.single();
	}

	public Flux<OrderTrackerResponseCanonical>  synchronizeOrderStatus(List<OrderSynchronizeDto> ordersList) {
		log.info("[START] synchronizeOrderStatus list:{}",ordersList);

		List<IOrderFulfillment> iOrdersFulfillment = getOrderLightByecommercesIds(
															ordersList
																	.stream()
																	.map(OrderSynchronizeDto::getEcommerceId)
																	.collect(Collectors.toSet()));

		return Flux
				.fromIterable(iOrdersFulfillment)
				.flatMap(iorder -> {
					OrderSynchronizeDto orderSynchronizeDto = ordersList
																.stream()
																.filter(o -> o.getEcommerceId().equals(iorder.getEcommerceId()))
																.findFirst()
																.get();

					return Flux
							.fromIterable(orderSynchronizeDto.getHistory())
							.reduce((previous,current) ->  {

								if (Constant.ActionOrder.getByName(current.getAction()).getSequence()
									> Constant.ActionOrder.getByName(previous.getAction()).getSequence()) {

									return current;
								}

								return previous;

							})
                            .flatMap(statusLast -> {

                                // Se envía el último estado para que se registre en la DB fulfillment y su tracker

								ActionDto actionDto = ActionDto
														.builder()
														.action(statusLast.getAction())
														.origin(orderSynchronizeDto.getOrigin())
														.orderCancelCode(statusLast.getOrderCancelCode())
														.orderCancelObservation(statusLast.getOrderCancelObservation())
														.motorizedId(statusLast.getMotorizedId())
														.updatedBy(statusLast.getUpdatedBy())
														.actionDate(statusLast.getActionDate())
														.build();


								return updateTracker
										.sendOnlyLastStatusOrderFromSync(iorder, actionDto,
												orderCancellationService.evaluateGetCancel(actionDto)
										)
										.flatMap(orderCanonical -> {
											orderCanonical.setAction(statusLast.getAction());

											return Mono.just(orderCanonical);
										});
							})
							.flatMap(orderStatusLast -> {

                                return Flux
                                        .fromIterable(orderSynchronizeDto.getHistory())
                                        .sort(Comparator. comparing(obj -> Constant.ActionOrder.getByName(obj.getAction()).getSequence()))
                                        .flatMap(orderHistory -> updateTracker.updateOrderStatusListAudit(iorder, orderStatusLast, orderHistory, orderSynchronizeDto.getOrigin()))
                                        .filter(order ->  !order.getAction().equalsIgnoreCase(orderStatusLast.getAction()))
                                        .flatMap(order -> {

                                        	log.info("Sending status order to notification:{}",order.getEcommerceId());

											ActionDto actionDto = ActionDto
																		.builder()
																		.action(order.getAction())
																		.origin(orderSynchronizeDto.getOrigin())
																		.build();

                                            return processSendNotification(actionDto, iorder);

                                        })
										.defaultIfEmpty(true)
										.buffer()
										.flatMap(resultListStatus -> {
											OrderTrackerResponseCanonical orderTrackerResponseCanonical = new OrderTrackerResponseCanonical();
											orderTrackerResponseCanonical.setEcommerceId(iorder.getEcommerceId());
											orderTrackerResponseCanonical.setStatusCode(Constant.OrderTrackerResponseCode.SUCCESS_CODE);

											return Flux.just(orderTrackerResponseCanonical);
										}).single();
							});
				});

	}

	public Mono<OrderCanonical> getOrderByEcommerceId(Long ecommerceId) {

		return Optional.ofNullable(getOrderFromIOrdersProjects(ecommerceId)).map(Mono::just) .orElse(Mono.empty());

	}

	public Flux<OrderCanonical> getOrderByEcommerceIds(Set<Long> ecommerceIds) {
		log.info("getOrderByEcommerceIds:{}",ecommerceIds);

		return Flux
				.fromIterable(getOrderLightByecommercesIds(ecommerceIds))
				.parallel()
				.runOn(Schedulers.elastic())
				.flatMap(orders -> Flux.just(getOrderFromIOrdersProjects(orders)))
				.ordered((o1, o2) -> o2.getEcommerceId().intValue() - o1.getEcommerceId().intValue());

	}

	private Flux<OrderCanonical> getOrdersByEcommerceIds(Set<Long> ecommerceIds) {
		log.info("getOrdersByEcommerceId:{}",ecommerceIds);

		return Flux
				.fromIterable(getOrderByEcommercesIds(ecommerceIds))
				.parallel()
				.runOn(Schedulers.elastic())
				.flatMap(orders -> Flux.just(getOrderToOrderTracker(orders)))
				.ordered((o1, o2) -> o2.getEcommerceId().intValue() - o1.getEcommerceId().intValue());

	}

	private void processErrorOrdersToSendAudit(String errorDetail, List<Long> ecommercesIds, String updateBy) {

        ecommercesIds.stream().forEach(val -> {
            OrderCanonical orderCanonical = new OrderCanonical();
            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
            orderStatus.setCode(Constant.OrderStatus.ERROR_ASSIGNED.getCode());
            orderStatus.setName(Constant.OrderStatus.ERROR_ASSIGNED.name());
            orderStatus.setDetail(errorDetail);
            orderCanonical.setOrderStatus(orderStatus);
            orderCanonical.setEcommerceId(val);
            iAuditAdapter.updateAudit(orderCanonical, updateBy);
        });

    }

}
