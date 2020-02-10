package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
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

import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class OrderProcessFacade {

    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalServiceDispatcher;
    private OrderExternalService orderExternalServiceInkatrackerLite;

    public OrderProcessFacade(OrderTransaction orderTransaction,
                              ObjectToMapper objectToMapper,
                              @Qualifier("deliveryDispatcher") OrderExternalService orderExternalServiceDispatcher,
                              @Qualifier("inkatrackerlite") OrderExternalService orderExternalServiceInkatrackerLite) {
        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.orderExternalServiceDispatcher = orderExternalServiceDispatcher;
        this.orderExternalServiceInkatrackerLite = orderExternalServiceInkatrackerLite;
    }

    public OrderCanonical createOrder(OrderDto orderDto){

        log.info("[START] createOrder facade");

        ServiceLocalOrder serviceLocalOrderEntity =
                orderTransaction
                        .createOrder(
                                objectToMapper.convertOrderdtoToOrderEntity(orderDto), orderDto
                        );

        OrderCanonical orderCanonical = objectToMapper.convertEntityToOrderCanonical(serviceLocalOrderEntity);
        log.info("[END] createOrder facade");
        return orderCanonical;
    }


    public OrderCanonical getUpdateOrder(ActionDto actionDto, String ecommerceId) {
        log.info("[START] getUpdateOrder action:{}",actionDto);

        Long ecommercePurchaseId = Long.parseLong(ecommerceId);
        OrderCanonical resultCanonical;

        IOrderFulfillment iOrderFulfillment = orderTransaction.getOrderByecommerceId(ecommercePurchaseId);

        int attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).orElse(0);
        int attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0);

        if (Optional.ofNullable(iOrderFulfillment.getOrderId()).isPresent()) {

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
                    resultCanonical.setAttempt(attempt);
                    resultCanonical.setAttemptTracker(attemptTracker);

                    break;
                case 2:
                    // Result of call to reattempt to insink
                    resultCanonical = orderExternalServiceDispatcher
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    log.info("Action value {} ",Constant.ActionOrder.getByName(actionDto.getAction()).getCode());

                    attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).orElse(0) + 1;

                    if (!resultCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())) {
                        attemptTracker = Optional.ofNullable(resultCanonical.getAttemptTracker()).orElse(0) + 1;
                    }

                    orderTransaction.updateOrderRetrying(
                            iOrderFulfillment.getOrderId(), attempt, attemptTracker,
                            resultCanonical.getOrderStatus().getCode(), resultCanonical.getOrderStatus().getDetail(),
                            Optional.ofNullable(resultCanonical.getExternalId()).orElse(null),
                            Optional.ofNullable(resultCanonical.getTrackerId()).orElse(null)
                    );
                    resultCanonical.setAttempt(attempt);
                    resultCanonical.setAttemptTracker(attemptTracker);
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
                    resultCanonical.setAttempt(attempt);
                    resultCanonical.setAttemptTracker(attemptTracker);
                    break;

                case 4:
                    // call the service inkatracker-lite to update the order status (CANCEL, READY_FOR_PICKUP, DELIVERED)
                    resultCanonical = orderExternalServiceInkatrackerLite
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    orderTransaction.updateStatusOrder(iOrderFulfillment.getOrderId(), resultCanonical.getOrderStatus().getCode(),
                            resultCanonical.getOrderStatus().getDetail());

                    if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

                        OrderFulfillment orderFulfillment = new OrderFulfillment();
                        orderFulfillment.setId(iOrderFulfillment.getOrderId());

                        CancellationCodeReason cancellationCodeReason = new CancellationCodeReason();
                        cancellationCodeReason.setCode(actionDto.getOrderCancelCode());

                        OrderCancelled orderCancelled = new OrderCancelled();
                        OrderCancelledIdentity orderCancelledIdentity = new OrderCancelledIdentity();
                        orderCancelledIdentity.setOrderFulfillment(orderFulfillment);
                        orderCancelled.setOrderCancelledIdentity(orderCancelledIdentity);
                        orderCancelled.setCancellationCodeReason(cancellationCodeReason);
                        orderCancelled.setObservation(actionDto.getOrderCancelObservation());

                        orderTransaction.insertCancelledOrder(orderCancelled);
                    }

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
