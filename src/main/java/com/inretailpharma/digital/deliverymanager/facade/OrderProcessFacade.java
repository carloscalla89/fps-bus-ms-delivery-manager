package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.events.KafkaEvent;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.Optional;


@Slf4j
@Component
public class OrderProcessFacade {

    private OrderTransaction orderTransaction;
    private KafkaEvent kafkaEvent;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalServiceDispatcher;
    private OrderExternalService orderExternalServiceInkatrackerLite;

    public OrderProcessFacade(OrderTransaction orderTransaction, KafkaEvent kafkaEvent,
                              ObjectToMapper objectToMapper,
                              @Qualifier("deliveryDispatcher") OrderExternalService orderExternalServiceDispatcher,
                              @Qualifier("inkatrackerlite") OrderExternalService orderExternalServiceInkatrackerLite) {
        this.orderTransaction = orderTransaction;
        this.kafkaEvent = kafkaEvent;
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

        OrderCanonical orderCanonical =
                objectToMapper
                        .convertIOrderDtoToOrderFulfillmentCanonical(
                                orderTransaction.getOrderByecommerceId(ecommercePurchaseId)
                        );
        int attemptTracker = Optional.ofNullable(orderCanonical.getAttemptTracker()).orElse(0);
        int attempt = Optional.ofNullable(orderCanonical.getAttempt()).orElse(0);

        if (Optional.ofNullable(orderCanonical.getId()).isPresent()) {

            switch (Constant.ActionOrder.getByName(actionDto.getAction()).getCode()) {

                case 1:
                    // Result of call to reattempt at inkatracker
                    resultCanonical = orderExternalServiceDispatcher
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    log.info("Action value {} ",Constant.ActionOrder.getByName(actionDto.getAction()).getCode());

                    attemptTracker = attemptTracker + 1;

                    orderTransaction.updateOrderRetryingTracker(
                            orderCanonical.getId(), attemptTracker,
                            resultCanonical.getOrderStatus().getCode(), resultCanonical.getOrderStatus().getDetail(),
                            Optional.ofNullable(resultCanonical.getTrackerId()).orElse(null)
                    );
                    resultCanonical.setExternalId(orderCanonical.getExternalId());
                    resultCanonical.setAttempt(attempt);
                    resultCanonical.setAttemptTracker(attemptTracker);

                    break;
                case 2:
                    // Result of call to reattempt to insink
                    resultCanonical = orderExternalServiceDispatcher
                            .getResultfromExternalServices(ecommercePurchaseId, actionDto);

                    log.info("Action value {} ",Constant.ActionOrder.getByName(actionDto.getAction()).getCode());

                    attempt = Optional.ofNullable(orderCanonical.getAttempt()).orElse(0) + 1;

                    if (!resultCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())) {
                        attemptTracker = Optional.ofNullable(resultCanonical.getAttemptTracker()).orElse(0) + 1;
                    }

                    orderTransaction.updateOrderRetrying(
                            orderCanonical.getId(), attempt, attemptTracker,
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
                        attemptTracker = Optional.ofNullable(orderCanonical.getAttemptTracker()).orElse(0) + 1;
                    }

                    orderTransaction.updateReservedOrder(
                            orderCanonical.getId(),
                            Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null),
                            attempt,
                            orderStatusEntity.getCode(),
                            actionDto.getOrderStatusDto().getDescription()
                    );

                    resultCanonical = new OrderCanonical();

                    resultCanonical.setEcommerceId(orderCanonical.getEcommerceId());
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

                    orderTransaction.updateStatusOrder(orderCanonical.getId(), resultCanonical.getOrderStatus().getCode(),
                            resultCanonical.getOrderStatus().getDetail());

                    resultCanonical.setEcommerceId(ecommercePurchaseId);
                    resultCanonical.setExternalId(orderCanonical.getExternalId());
                    resultCanonical.setTrackerId(orderCanonical.getTrackerId());
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



}
