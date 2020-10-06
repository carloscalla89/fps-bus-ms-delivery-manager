package com.inretailpharma.digital.deliverymanager.facade;

import java.util.Optional;
import java.util.stream.Collectors;

import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderDetailCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.entity.OrderWrapperResponse;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;

import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;

import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class DeliveryManagerFacade {

    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalServiceAudit;
    private CenterCompanyService centerCompanyService;
    private OrderCancellationService orderCancellationService;
    private final ApplicationContext context;

    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 ObjectToMapper objectToMapper,
                                 @Qualifier("audit") OrderExternalService orderExternalServiceAudit,
                                 ApplicationContext context,
                                 CenterCompanyService centerCompanyService,
                                 OrderCancellationService orderCancellationService) {

        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.orderExternalServiceAudit = orderExternalServiceAudit;
        this.centerCompanyService = centerCompanyService;
        this.orderCancellationService = orderCancellationService;
        this.context = context;

    }


    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {

        log.info("[START] createOrder facade:{}",orderDto);

        return Mono
                .defer(() -> centerCompanyService.getExternalInfo(orderDto.getCompanyCode(), orderDto.getLocalCode()))
                .zipWith(Mono.just(objectToMapper.convertOrderdtoToOrderEntity(orderDto)), (storeCenter,orderFulfillment) -> {

                    OrderWrapperResponse wrapperResponse =  orderTransaction.createOrderTransaction(
                            orderFulfillment,
                            orderDto,
                            storeCenter
                    );

                    OrderCanonical orderCanonicalResponse =  objectToMapper
                                                                    .setsOrderWrapperResponseToOrderCanonical(wrapperResponse, orderDto);

                    orderExternalServiceAudit
                            .sendOrderReactive(orderCanonicalResponse)
                            .subscribe();

                    return orderCanonicalResponse;
                })
                .flatMap(r -> {
                    log.info("[START] Preparation to send order some tracker with service-detail:{} and ecommerceId:{}",
                            r.getOrderDetail(), r.getEcommerceId());

                    if (r.getOrderDetail().isServiceEnabled()
                            && (Constant.OrderStatus.getByCode(r.getOrderStatus().getCode()).isSuccess()))
                    {

                        OrderExternalService orderExternalServiceTracker = (OrderExternalService)context.getBean(
                                Constant.TrackerImplementation.getByCode(r.getOrderDetail().getServiceCode()).getName()
                        );

                        return orderExternalServiceTracker
                                .sendOrderToTracker(r)
                                .flatMap(s -> {

                                    orderTransaction.updateOrderRetryingTracker(
                                            s.getId(), Optional.ofNullable(s.getAttemptTracker()).orElse(0)+1,
                                            s.getOrderStatus().getCode(), s.getOrderStatus().getDetail(), s.getTrackerId());

                                    orderExternalServiceAudit
                                            .updateOrderReactive(s)
                                            .subscribe(rs -> log.info("result audit:{}",rs));

                                    return Mono.just(s);
                                });

                    }
                    log.info("[END] Preparation to send order some tracker with service-detail:{} and ecommerceId:{}",
                            r.getOrderDetail(), r.getEcommerceId());
                    return Mono.just(r);
                })
                .doOnSuccess(r -> log.info("[END] createOrder facade"));

    }

    public Mono<OrderCanonical> getUpdateOrder(ActionDto actionDto, String ecommerceId) {
        log.info("[START] getUpdateOrder action:{}",actionDto);

        Long ecommercePurchaseId = Long.parseLong(ecommerceId);

        IOrderFulfillment iOrderFulfillment = orderTransaction.getOrderByecommerceId(ecommercePurchaseId);
        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        if (Optional.ofNullable(iOrderFulfillment).isPresent()
                && !Constant.OrderStatus.getFinalStatusByCode(iOrderFulfillment.getStatusCode())) {

            OrderDetailCanonical orderDetail = new OrderDetailCanonical();

            switch (action.getCode()) {
                case 1:

                    OrderExternalService orderServiceTracker = (OrderExternalService)context.getBean(
                            Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getName()
                    );

                    if (Constant.ActionOrder.ATTEMPT_TRACKER_CREATE.name().equalsIgnoreCase(actionDto.getAction())) {

                        return centerCompanyService
                                .getExternalInfo(iOrderFulfillment.getCompanyCode(), iOrderFulfillment.getCenterCode())
                                .flatMap(r -> {
                                    OrderCanonical orderCanonical =  objectToMapper
                                            .convertIOrderDtoToAndItemOrderFulfillmentCanonical(
                                                    iOrderFulfillment,
                                                    orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId())
                                            );
                                    orderCanonical.setLocalId(r.getLegacyId());
                                    orderCanonical.setLocalCode(r.getLocalCode());
                                    orderCanonical.setLocal(r.getName());
                                    orderCanonical.setLocalDescription(r.getDescription());
                                    orderCanonical.setLocalLatitude(r.getLatitude());
                                    orderCanonical.setLocalLongitude(r.getLongitude());
                                    orderCanonical.setCompanyCode(r.getCompanyCode());
                                    return Mono.just(orderCanonical);

                                })
                                .flatMap(r ->{
                                    // creando la orden a tracker

                                    r.setAction(action.name()); // set action to return the status at some tracker

                                    return orderServiceTracker
                                            .sendOrderToTracker(r)
                                            .flatMap(s -> {
                                                OrderCanonical orderCanonical = processTransaction(iOrderFulfillment, s);
                                                return Mono.just(orderCanonical);
                                            });
                                });

                    } else {
                        // actualizando la orden a tracker

                        actionDto
                                .setExternalBillingId(
                                        Optional.ofNullable(iOrderFulfillment.getExternalId())
                                                .map(String::valueOf)
                                                .orElse(actionDto.getExternalBillingId())
                                );

                        return  orderServiceTracker
                                    .getResultfromExternalServices(ecommercePurchaseId, actionDto, iOrderFulfillment.getCompanyCode())
                                    .flatMap(r -> {
                                        OrderCanonical orderCanonical = processTransaction(iOrderFulfillment, r);

                                        return Mono.just(orderCanonical);
                                    });
                    }
                case 2:

                    OrderExternalService orderExternalServiceDispatcher = (OrderExternalService)context.getBean(
                            Constant.DispatcherImplementation.getByCompanyCode(iOrderFulfillment.getCompanyCode()).getName()
                    );

                    // Reattempt to send the order at insink
                    return orderExternalServiceDispatcher
                                            .sendOrderEcommerce(
                                                    iOrderFulfillment,
                                                    orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId()),
                                                    action.name()
                                            )
                                            .flatMap(orderResp -> {

                                                if ((Constant
                                                        .OrderStatus
                                                        .getByCode(Optional
                                                                    .ofNullable(orderResp.getOrderStatus())
                                                                    .map(OrderStatusCanonical::getCode)
                                                                    .orElse(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                                                        ).isSuccess())) {

                                                    OrderCanonical order = objectToMapper.convertIOrderDtoToOrderFulfillmentCanonical(iOrderFulfillment);
                                                    order.setExternalId(orderResp.getExternalId());

                                                    OrderExternalService serviceTracker = (OrderExternalService)context.getBean(
                                                            Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getName()
                                                    );

                                                    return serviceTracker
                                                                .sendOrderToTracker(order)
                                                                .flatMap(s -> Mono.just(processTransaction(iOrderFulfillment, s)));

                                                } else {

                                                    return Mono.just(processTransaction(iOrderFulfillment, orderResp));

                                                }

                                            });

                case 3:
                    // Update the status when the order was released from Dispatcher
                    log.info("Starting to update the released order when the order come from dispatcher:{}",ecommerceId);

                    int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0);
                    int attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0) +1;

                    OrderDto orderDto = new OrderDto();
                    orderDto.setExternalPurchaseId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));
                    orderDto.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    orderDto.setOrderStatusDto(actionDto.getOrderStatusDto());

                    OrderStatus orderStatusEntity =  orderTransaction.getStatusOrderFromDeliveryDispatcher(orderDto);

                    if (!orderStatusEntity.getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RELEASE_DISPATCHER_ORDER.getCode())) {
                        attemptTracker = attemptTracker + 1;
                    }

                    orderTransaction.updateReservedOrder(
                            iOrderFulfillment.getOrderId(),
                            Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null),
                            attempt,
                            orderStatusEntity.getCode(),
                            actionDto.getOrderStatusDto().getDescription()
                    );

                    OrderCanonical result = new OrderCanonical();

                    result.setEcommerceId(iOrderFulfillment.getEcommerceId());

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(orderStatusEntity.getCode());
                    orderStatus.setName(orderStatusEntity.getType());
                    orderStatus.setDetail(actionDto.getOrderStatusDto().getDescription());
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                    result.setOrderStatus(orderStatus);

                    result.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    result.setExternalId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));


                    orderDetail.setAttempt(attempt);
                    orderDetail.setAttemptTracker(attemptTracker);

                    result.setOrderDetail(orderDetail);

                    result.setPurchaseId(Optional.ofNullable(iOrderFulfillment.getPurchaseId()).map(Integer::longValue).orElse(null));

                    orderExternalServiceAudit.updateOrderReactive(result).subscribe();

                    return Mono.just(result);

                case 4:
                    // call the service inkatracker-lite or inkatracker to update the order status (CANCEL, READY_FOR_PICKUP, DELIVERED)
                    log.info("Service Type Code:{}",iOrderFulfillment.getServiceTypeCode());

                    OrderExternalService orderExternalService2 = (OrderExternalService)context.getBean(
                            Constant.TrackerImplementation.getByCode(iOrderFulfillment.getServiceTypeCode()).getName()
                    );

                    if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {
                        CancellationCodeReason codeReason;
                        if (actionDto.getOrderCancelCode() != null && actionDto.getOrderCancelAppType() != null) {
                            codeReason = orderCancellationService
                                            .geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType());
                        } else {
                            codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
                        }

                        Optional.ofNullable(codeReason)
                                .ifPresent(r -> {
                                    actionDto.setOrderCancelAppType(r.getAppType());
                                    actionDto.setOrderCancelReason(r.getReason());
                                    actionDto.setOrderCancelClientReason(r.getClientReason());
                                });
                    }

                    actionDto.setExternalBillingId(Optional.ofNullable(iOrderFulfillment.getExternalId()).map(Object::toString).orElse(null));

                    return orderExternalService2
                                            .getResultfromExternalServices(ecommercePurchaseId, actionDto, iOrderFulfillment.getCompanyCode())
                                            .map(r -> {

                                                log.info("[START] to update order");

                                                if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {
                                                    log.info("Action to cancel order");
                                                    orderTransaction.updateStatusCancelledOrder(
                                                            r.getOrderStatus().getDetail(), actionDto.getOrderCancelObservation(),
                                                            actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType(),
                                                            r.getOrderStatus().getCode(), iOrderFulfillment.getOrderId()
                                                    );
                                                } else {
                                                    orderTransaction.updateStatusOrder(iOrderFulfillment.getOrderId(), r.getOrderStatus().getCode(),
                                                            r.getOrderStatus().getDetail());
                                                }

                                                log.info("[END] to update order");

                                                r.setEcommerceId(ecommercePurchaseId);
                                                r.setExternalId(iOrderFulfillment.getExternalId());
                                                r.setTrackerId(iOrderFulfillment.getTrackerId());
                                                r.setPurchaseId(Optional.ofNullable(iOrderFulfillment.getPurchaseId()).map(Integer::longValue).orElse(null));
                                                r.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

                                                orderExternalServiceAudit.updateOrderReactive(r).subscribe();

                                                return r;
                                            });


                default:
                    OrderCanonical resultDefault = new OrderCanonical();
                    OrderStatusCanonical orderStatusNotFound = new OrderStatusCanonical();
                    orderStatusNotFound.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    orderStatusNotFound.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    orderStatusNotFound.setStatusDate(DateUtils.getLocalDateTimeNow());
                    resultDefault.setOrderStatus(orderStatusNotFound);

                    resultDefault.setEcommerceId(ecommercePurchaseId);

                    return Mono.just(resultDefault);
            }

        } else {

            OrderStatusCanonical orderStatus =  Optional
                    .ofNullable(iOrderFulfillment)
                    .map(s -> {

                        OrderStatusCanonical os = new OrderStatusCanonical();
                        os.setCode(Constant.OrderStatus.END_STATUS_RESULT.getCode());
                        os.setName(Constant.OrderStatus.END_STATUS_RESULT.name());
                        os.setDetail("The order cant reattempted");
                        os.setStatusDate(DateUtils.getLocalDateTimeNow());

                        log.info("The order has end status:{}",os);

                        return os;

                    }).orElseGet(() -> {
                        OrderStatusCanonical os = new OrderStatusCanonical();
                        os.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
                        os.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
                        os.setDetail("Order not found");
                        os.setStatusDate(DateUtils.getLocalDateTimeNow());

                        log.info("Order not found:{}",os);

                        return os;
                    });

            OrderCanonical resultWithoutAction = new OrderCanonical();

            resultWithoutAction.setOrderStatus(orderStatus);
            resultWithoutAction.setEcommerceId(ecommercePurchaseId);

            return Mono.just(resultWithoutAction);
        }



    }


    private OrderCanonical processTransaction(IOrderFulfillment iOrderFulfillment, OrderCanonical r) {

        Integer attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).map(n -> n+1).orElse(null);
        Integer attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).map(n -> n+1).orElse(null);

        r.setExternalId(Optional.ofNullable(iOrderFulfillment.getExternalId())
                .orElse(r.getExternalId())
        );

        r.setTrackerId(Optional.ofNullable(iOrderFulfillment.getTrackerId())
                .orElse(r.getTrackerId())
        );

        r.setPurchaseId(Optional.ofNullable(iOrderFulfillment.getPurchaseId()).map(Integer::longValue).orElse(null));

        orderTransaction.updateOrderRetrying(
                iOrderFulfillment.getOrderId(), attempt, attemptTracker,
                r.getOrderStatus().getCode(), r.getOrderStatus().getDetail(),
                r.getExternalId(), r.getTrackerId()
        );

        OrderDetailCanonical orderDetail = new OrderDetailCanonical();
        orderDetail.setAttempt(attempt);
        orderDetail.setAttemptTracker(attemptTracker);

        r.setOrderDetail(orderDetail);

        r.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

        orderExternalServiceAudit.updateOrderReactive(r).subscribe();

        return r;
    }

	public Mono<OrderResponseCanonical> getOrderByOrderNumber(Long orderNumber) {
		log.info("START CALL FACADE getOrderByOrderNumber:"+orderNumber);
    	return Mono.fromCallable(() -> orderTransaction.getOrderByOrderNumber(orderNumber))		
		.flatMap(x -> { 
			log.info("x.isPresent()--->:"+x.isPresent());
			if(!x.isPresent()) return Mono.empty();	
			log.info("x.get()--->:"+x.get());
			IOrderResponseFulfillment orderResponseFulfillment=x.get();	    
			log.info("OrderResponseFulfillment--->:"+orderResponseFulfillment);
			OrderResponseCanonical orderResponseCanonical = OrderResponseCanonical.builder()
                .scheduledOrderDate(orderResponseFulfillment.getScheduledOrderDate())
				.payOrderDate(orderResponseFulfillment.getPayOrderDate())
				.transactionOrderDate(orderResponseFulfillment.getTransactionOrderDate())				
				.purchaseNumber(orderResponseFulfillment.getPurchaseNumber())
                .posCode(orderResponseFulfillment.getPosCode())
                .creditCardId(orderResponseFulfillment.getCreditCardId())
                .paymentMethodId(orderResponseFulfillment.getPaymentMethodId())
                .confirmedOrder(orderResponseFulfillment.getConfirmedOrder())
				.build();
			log.info("END FACADE getOrderByOrderNumber:"+orderNumber);
			return Mono.just(orderResponseCanonical);
		})
		.onErrorResume(e -> {
			log.info("ERROR ON CALL ORDEN TRANSACTION");
			throw new RuntimeException("Error on get order by orderNumber..."+e);
		});
    }

    public Mono<OrderCanonical> getUpdatePartialOrder(OrderDto partialOrderDto) {
        log.info("[START getUpdatePartialOrder]");
        log.info("request partialOrderDto: {}",partialOrderDto);
        try {
            return Mono.just(orderTransaction.updatePartialOrder(partialOrderDto));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("ERROR at updating the order:{}",e.getMessage());
            OrderCanonical resultDefault = new OrderCanonical();
            OrderStatusCanonical orderStatusNotFound = new OrderStatusCanonical();
            orderStatusNotFound.setCode(Constant.OrderTrackerResponseCode.ERROR_CODE);
            orderStatusNotFound.setName(Constant.OrderTrackerResponseCode.ERROR_CODE);
            orderStatusNotFound.setStatusDate(DateUtils.getLocalDateTimeNow());
            return Mono.just(resultDefault);
        }
    }

}
