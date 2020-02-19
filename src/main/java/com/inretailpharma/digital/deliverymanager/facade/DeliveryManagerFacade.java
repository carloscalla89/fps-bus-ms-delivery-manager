package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class DeliveryManagerFacade {

    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalServiceDispatcher;
    private OrderExternalService orderExternalServiceInkatrackerLite;
    private OrderExternalService orderExternalServiceOrderTracker;
    private OrderExternalService orderExternalServiceAudit;

    public DeliveryManagerFacade(OrderTransaction orderTransaction,
                                 ObjectToMapper objectToMapper,
                                 @Qualifier("deliveryDispatcher") OrderExternalService orderExternalServiceDispatcher,
                                 @Qualifier("inkatrackerlite") OrderExternalService orderExternalServiceInkatrackerLite,
                                 @Qualifier("orderTracker") OrderExternalService orderExternalServiceOrderTracker,
                                 @Qualifier("audit") OrderExternalService orderExternalServiceAudit) {
        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.orderExternalServiceDispatcher = orderExternalServiceDispatcher;
        this.orderExternalServiceInkatrackerLite = orderExternalServiceInkatrackerLite;
        this.orderExternalServiceOrderTracker = orderExternalServiceOrderTracker;
        this.orderExternalServiceAudit = orderExternalServiceAudit;
    }

    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {

        log.info("[START] createOrder facade");

        return Mono
                .defer(() -> Mono.just(objectToMapper.convertOrderdtoToOrderEntity(orderDto)))
                .zipWith(
                        objectToMapper.convertEntityToOrderCanonical(orderDto), (a,b) ->
                        {

                            OrderWrapperResponse r =  orderTransaction.createOrderTransaction(a, orderDto);

                            // set tracker ID
                            b.setTrackerId(r.getTrackerId());

                            // set status
                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                            orderStatus.setCode(r.getOrderStatusCode());
                            orderStatus.setName(r.getOrderStatusName());
                            orderStatus.setDetail(r.getOrderStatusDetail());
                            b.setOrderStatus(orderStatus);

                            // set service of delivery or pickup on store
                            b.getOrderDetail().setServiceCode(r.getServiceCode());
                            b.getOrderDetail().setServiceName(r.getServiceName());
                            b.getOrderDetail().setServiceType(r.getServiceType());
                            b.getOrderDetail().setAttempt(r.getAttemptBilling());
                            b.getOrderDetail().setAttemptTracker(r.getAttemptTracker());

                            // set local and company names;
                            b.setCompany(r.getCompanyName());
                            b.setLocal(r.getLocalName());

                            // set Receipt
                            b.getReceipt().setType(r.getReceiptName());

                            // set Payment
                            b.getPaymentMethod().setType(r.getPaymentMethodName());

                            Mono.just(b).subscribe(au -> orderExternalServiceAudit.sendOrderReactive(au));

                            return b;
                        })
                .flatMap(r ->
                        Mono.just(r).zipWith(orderExternalServiceOrderTracker.sendOrderReactiveWithOrderDto(r), (a, b) -> {

                            a.setOrderStatus(b.getOrderStatus());

                            orderExternalServiceAudit.updateOrderReactive(a).subscribeOn(Schedulers.parallel());

                            return a;
                })).doOnSuccess(r -> log.info("[END] createOrder facade"));

    }

    public OrderCanonical getUpdateOrder(ActionDto actionDto, String ecommerceId) {
        log.info("[START] getUpdateOrder action:{}",actionDto);

        Long ecommercePurchaseId = Long.parseLong(ecommerceId);
        OrderCanonical resultCanonical;

        IOrderFulfillment iOrderFulfillment = orderTransaction.getOrderByecommerceId(ecommercePurchaseId);

        if (Optional.ofNullable(iOrderFulfillment).isPresent()) {

            OrderDetailCanonical orderDetail = new OrderDetailCanonical();

            int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0);
            int attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0);

            switch (Constant.ActionOrder.getByName(actionDto.getAction()).getCode()) {

                case 1:
                    // Result of call to reattempt at inkatracker
                    resultCanonical = orderExternalServiceDispatcher
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    log.info("Action value {} ",Constant.ActionOrder.getByName(actionDto.getAction()).getCode());

                    attemptTracker = attemptTracker + 1;

                    orderTransaction.updateOrderRetryingTracker(
                            iOrderFulfillment.getOrderId(), attemptTracker,
                            resultCanonical.getOrderStatus().getCode(), resultCanonical.getOrderStatus().getDetail(),
                            Optional.ofNullable(resultCanonical.getTrackerId()).orElse(null)
                    );
                    resultCanonical.setExternalId(iOrderFulfillment.getExternalId());

                    orderDetail.setAttempt(attempt);
                    orderDetail.setAttemptTracker(attemptTracker);
                    resultCanonical.setOrderDetail(orderDetail);

                    break;
                case 2:
                    // Result of call to reattempt to insink
                    resultCanonical = orderExternalServiceDispatcher
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    log.info("Action value {} ",Constant.ActionOrder.getByName(actionDto.getAction()).getCode());

                    attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0) + 1;

                    if (!resultCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())) {
                        attemptTracker = Optional.ofNullable(resultCanonical.getOrderDetail().getAttemptTracker()).orElse(0) + 1;
                    }

                    orderTransaction.updateOrderRetrying(
                            iOrderFulfillment.getOrderId(), attempt, attemptTracker,
                            resultCanonical.getOrderStatus().getCode(), resultCanonical.getOrderStatus().getDetail(),
                            Optional.ofNullable(resultCanonical.getExternalId()).orElse(null),
                            Optional.ofNullable(resultCanonical.getTrackerId()).orElse(null)
                    );

                    orderDetail.setAttempt(attempt);
                    orderDetail.setAttemptTracker(attemptTracker);

                    resultCanonical.setOrderDetail(orderDetail);
                    break;

                case 3:
                    // Update the status when the order was released in Dispatcher

                    log.info("Starting to update the released order when the order come from dispatcher");

                    OrderDto orderDto = new OrderDto();
                    orderDto.setExternalPurchaseId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));
                    orderDto.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    orderDto.setOrderStatusDto(actionDto.getOrderStatusDto());

                    OrderStatus orderStatusEntity =  orderTransaction.getStatusOrderFromDeliveryDispatcher(orderDto);

                    attempt = attempt + 1;

                    if (!orderStatusEntity.getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode())) {
                        attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0) + 1;
                    }

                    orderTransaction.updateReservedOrder(
                            iOrderFulfillment.getOrderId(),
                            Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null),
                            attempt,
                            orderStatusEntity.getCode(),
                            actionDto.getOrderStatusDto().getDescription()
                    );

                    resultCanonical = new OrderCanonical();

                    resultCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                    resultCanonical.getOrderStatus().setCode(orderStatusEntity.getCode());
                    resultCanonical.getOrderStatus().setName(orderStatusEntity.getType());
                    resultCanonical.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    resultCanonical.setExternalId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));
                    resultCanonical.getOrderStatus().setDetail(actionDto.getOrderStatusDto().getDescription());

                    orderDetail.setAttempt(attempt);
                    orderDetail.setAttemptTracker(attemptTracker);

                    resultCanonical.setOrderDetail(orderDetail);

                    break;

                case 4:
                    // call the service inkatracker-lite to update the order status (CANCEL, READY_FOR_PICKUP, DELIVERED)
                    resultCanonical = orderExternalServiceInkatrackerLite
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    orderTransaction.updateStatusOrder(iOrderFulfillment.getOrderId(), resultCanonical.getOrderStatus().getCode(),
                            resultCanonical.getOrderStatus().getDetail());

                    log.info("[START] to registar cancelled order");
                    if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

                        OrderFulfillment orderFulfillment = orderTransaction.getOrderFulfillmentById(iOrderFulfillment.getOrderId());
                        OrderCancelledIdentity orderCancelledIdentity = new OrderCancelledIdentity();
                        orderCancelledIdentity.setOrderFulfillment(orderFulfillment);

                        CancellationCodeReason codeReason = orderTransaction.getCancellationCodeReasonByCode(actionDto.getOrderCancelCode());

                        OrderCancelled orderCancelled = new OrderCancelled();
                        orderCancelled.setOrderCancelledIdentity(orderCancelledIdentity);
                        orderCancelled.setCancellationCodeReason(codeReason);
                        orderCancelled.setObservation(actionDto.getOrderCancelObservation());

                        orderTransaction.insertCancelledOrder(orderCancelled);
                    }
                    log.info("[START] to registar cancelled order");
                    resultCanonical.setEcommerceId(ecommercePurchaseId);
                    resultCanonical.setExternalId(iOrderFulfillment.getExternalId());
                    resultCanonical.setTrackerId(iOrderFulfillment.getTrackerId());
                    break;

                default:
                    resultCanonical = new OrderCanonical();
                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    resultCanonical.setOrderStatus(orderStatus);
                    resultCanonical.setEcommerceId(ecommercePurchaseId);
                    break;

            }

        } else {
            resultCanonical = new OrderCanonical();
            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
            orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
            resultCanonical.setOrderStatus(orderStatus);
            resultCanonical.setEcommerceId(ecommercePurchaseId);
        }

        return resultCanonical;

    }

    public List<OrderCancellationCanonical> getOrderCancellationList() {
        return objectToMapper.convertEntityOrderCancellationToCanonical(orderTransaction.getListCancelReason());
    }

}
