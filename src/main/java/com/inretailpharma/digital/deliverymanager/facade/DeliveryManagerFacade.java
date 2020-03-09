package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


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

    public Flux<OrderCanonical> getOrdersByStatus(String status) {

        return Flux.fromIterable(orderTransaction.getOrdersByStatus(status).stream().map(r -> {
            OrderCanonical orderCanonical = new OrderCanonical();
            orderCanonical.setEcommerceId(r.getEcommerceId());
            orderCanonical.setExternalId(r.getExternalId());
            return orderCanonical;
        }).collect(Collectors.toList()));

    }



    public Mono<OrderCanonical> createOrder(OrderDto orderDto) {

        log.info("[START] createOrder facade");

        return Mono
                .defer(() -> Mono.just(objectToMapper.convertOrderdtoToOrderEntity(orderDto)))
                .zipWith(
                        objectToMapper.convertEntityToOrderCanonical(orderDto), (a,b) ->
                        {
                            OrderWrapperResponse r =  orderTransaction.createOrderTransaction(a, orderDto);

                            //set fulfillmentID
                            b.setId(r.getFulfillmentId());

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

                            // attempts
                            b.setAttemptTracker(r.getAttemptTracker());
                            b.setAttempt(r.getAttemptBilling());

                            b.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

                            orderExternalServiceAudit.sendOrderReactive(b).subscribe();

                            return b;
                        })
                .doOnSuccess(r -> log.info("[END] createOrder facade"));

    }

    public Mono<OrderCanonical> getUpdateOrder(ActionDto actionDto, String ecommerceId) {
        log.info("[START] getUpdateOrder action:{}",actionDto);

        Long ecommercePurchaseId = Long.parseLong(ecommerceId);
        OrderCanonical resultCanonical;
        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

        IOrderFulfillment iOrderFulfillment = orderTransaction.getOrderByecommerceId(ecommercePurchaseId);
        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        if (Optional.ofNullable(iOrderFulfillment).isPresent()) {

            OrderDetailCanonical orderDetail = new OrderDetailCanonical();

            int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0);
            int attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0);

            switch (action.getCode()) {

                case 1:
                    // Reattempt to send the order some inkatracker
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

                    orderExternalServiceAudit.updateOrderReactive(resultCanonical).subscribe();

                    break;
                case 2:
                    // Reattempt to send the order at insink
                    resultCanonical = orderExternalServiceDispatcher
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    log.info("Action value {} ",Constant.ActionOrder.getByName(actionDto.getAction()).getCode());

                    attempt = Optional.of(attempt).orElse(0) + 1;

                    if (!resultCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())) {
                        attemptTracker = Optional.of(attemptTracker).orElse(0) + 1;
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

                    orderExternalServiceAudit.updateOrderReactive(resultCanonical).subscribe();

                    break;

                case 3:
                    // Update the status when the order was released from Dispatcher

                    log.info("Starting to update the released order when the order come from dispatcher");

                    OrderDto orderDto = new OrderDto();
                    orderDto.setExternalPurchaseId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));
                    orderDto.setTrackerId(Optional.ofNullable(actionDto.getTrackerId()).map(Long::parseLong).orElse(null));
                    orderDto.setOrderStatusDto(actionDto.getOrderStatusDto());

                    OrderStatus orderStatusEntity =  orderTransaction.getStatusOrderFromDeliveryDispatcher(orderDto);

                    attempt = attempt + 1;

                    if (!orderStatusEntity.getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode())) {
                        attemptTracker = Optional.ofNullable(attemptTracker).orElse(0) + 1;
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

                    orderExternalServiceAudit.updateOrderReactive(resultCanonical).subscribe();

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

                    orderExternalServiceAudit.updateOrderReactive(resultCanonical).subscribe();

                    break;

                case 5:
                    log.info("[START] to update order");
                    resultCanonical = new OrderCanonical();

                    try {
                        // Update the status order
                        orderTransaction.updateStatusOrder(
                                iOrderFulfillment.getOrderId(),
                                action.getOrderSuccessStatusCode(),
                                null
                        );

                        orderStatus.setCode(Constant.OrderStatus.getByCode(action.getOrderSuccessStatusCode()).getCode());
                        orderStatus.setName(Constant.OrderStatus.getByCode(action.getOrderSuccessStatusCode()).name());
                    } catch(Exception e) {
                        e.printStackTrace();
                        log.error("Error during update the fulfillment delivery database:{}",e.getMessage());

                        // Update the status order
                        orderTransaction.updateStatusOrder(
                                iOrderFulfillment.getOrderId(),
                                Constant.ActionOrder.getByName(actionDto.getAction()).getOrderErrorStatusCode(),
                                e.getMessage()
                        );

                        orderStatus.setCode(Constant.OrderStatus.getByCode(action.getOrderErrorStatusCode()).getCode());
                        orderStatus.setName(Constant.OrderStatus.getByCode(action.getOrderErrorStatusCode()).name());
                        orderStatus.setDetail(e.getMessage());

                    }

                    resultCanonical.setOrderStatus(orderStatus);
                    resultCanonical.setEcommerceId(ecommercePurchaseId);

                    orderExternalServiceAudit.updateOrderReactive(resultCanonical).subscribe();

                    break;

                default:
                    resultCanonical = new OrderCanonical();

                    orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    resultCanonical.setOrderStatus(orderStatus);
                    resultCanonical.setEcommerceId(ecommercePurchaseId);
                    break;

            }

        } else {

            resultCanonical = new OrderCanonical();
            orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
            resultCanonical.setOrderStatus(orderStatus);
            resultCanonical.setEcommerceId(ecommercePurchaseId);
        }

        return Mono.just(resultCanonical);

    }

    public List<OrderCancellationCanonical> getOrderCancellationList() {
        return objectToMapper.convertEntityOrderCancellationToCanonical(orderTransaction.getListCancelReason());
    }

}
