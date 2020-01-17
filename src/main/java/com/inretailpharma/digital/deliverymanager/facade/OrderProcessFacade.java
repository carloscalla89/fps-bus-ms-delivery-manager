package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderProcessFacade {

    private OrderTransaction orderTransaction;
    private KafkaEvent kafkaEvent;
    private ObjectToMapper objectToMapper;
    private OrderExternalService orderExternalService;

    public OrderProcessFacade(OrderTransaction orderTransaction, KafkaEvent kafkaEvent,
                              ObjectToMapper objectToMapper,
                              @Qualifier("deliveryDispatcher") OrderExternalService orderExternalService) {
        this.orderTransaction = orderTransaction;
        this.kafkaEvent = kafkaEvent;
        this.objectToMapper = objectToMapper;
        this.orderExternalService = orderExternalService;
    }

    public OrderFulfillmentCanonical createOrder(OrderDto orderDto){

        try{
            log.info("[START] create order facade");

            ServiceLocalOrder serviceLocalOrderEntity =
                    orderTransaction
                            .createOrder(
                                    objectToMapper.convertOrderdtoToOrderEntity(orderDto), orderDto
                            );

            return objectToMapper.convertEntityToOrderFulfillmentCanonical(serviceLocalOrderEntity);

        }finally {
            log.info("[END] create order facade - orderFulfillmentCanonical");
        }

    }


    public OrderCanonical getUpdateOrder(String action, String ecommerceId, String externalId,
                                         String trackerId, OrderStatusDto orderStatusDto) {
        log.info("[START] getUpdateOrder action:{}",action);

        Long ecommercePurchaseId = Long.parseLong(ecommerceId);
        OrderCanonical resultCanonical;

        OrderFulfillmentCanonical orderFulfillment =
                objectToMapper
                        .convertIOrderDtoToOrderFulfillmentCanonical(
                                orderTransaction.getOrderByecommerceId(ecommercePurchaseId)
                        );
        int attemptTracker = Optional.ofNullable(orderFulfillment.getAttemptTracker()).orElse(0);
        int attempt = Optional.ofNullable(orderFulfillment.getAttempt()).orElse(0);

        if (Optional.ofNullable(orderFulfillment.getId()).isPresent()) {

            switch (Constant.ActionOrder.getByName(action).getCode()) {

                case 1:
                    // Result of call to reattempt at inkatracker
                    resultCanonical = orderExternalService
                            .getResultfromExternalServices(ecommercePurchaseId, Constant.ActionOrder.getByName(action));

                    log.info("Action value {} ",Constant.ActionOrder.getByName(action).getCode());

                    attemptTracker = attemptTracker + 1;

                    orderTransaction.updateOrderRetryingTracker(
                            orderFulfillment.getId(), attemptTracker,
                            resultCanonical.getStatusCode(), resultCanonical.getStatusDetail(),
                            Optional.ofNullable(resultCanonical.getTrackerId()).orElse(null)
                    );
                    resultCanonical.setExternalId(orderFulfillment.getExternalId());

                    break;
                case 2:
                    // Result of call to reattempt to insink
                    resultCanonical = orderExternalService
                            .getResultfromExternalServices(ecommercePurchaseId, Constant.ActionOrder.getByName(action));

                    log.info("Action value {} ",Constant.ActionOrder.getByName(action).getCode());

                    attempt = Optional.ofNullable(orderFulfillment.getAttempt()).orElse(0) + 1;

                    if (!resultCanonical.getStatusCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())) {
                        attemptTracker = Optional.ofNullable(orderFulfillment.getAttemptTracker()).orElse(0) + 1;
                    }

                    orderTransaction.updateOrderRetrying(
                            orderFulfillment.getId(), attempt, attemptTracker,
                            resultCanonical.getStatusCode(), resultCanonical.getStatusDetail(),
                            Optional.ofNullable(resultCanonical.getExternalId()).orElse(null),
                            Optional.ofNullable(resultCanonical.getTrackerId()).orElse(null)
                    );

                    break;

                case 3:
                    // Result to update the status when the order was released in Dispatcher

                    log.info("Starting to update release order when the order come from dispatcher");

                    OrderDto orderDto = new OrderDto();
                    orderDto.setExternalPurchaseId(Optional.ofNullable(externalId).map(Long::parseLong).orElse(null));
                    orderDto.setTrackerId(Optional.ofNullable(trackerId).map(Long::parseLong).orElse(null));
                    orderDto.setOrderStatusDto(orderStatusDto);

                    OrderStatus orderStatus =  orderTransaction.getStatusOrderFromDeliveryDispatcher(orderDto);

                    attempt = attempt + 1;

                    if (!orderStatus.getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode())) {
                        attemptTracker = Optional.ofNullable(orderFulfillment.getAttemptTracker()).orElse(0) + 1;
                    }

                    orderTransaction.updateReservedOrder(
                            orderFulfillment.getId(),
                            Optional.ofNullable(externalId).map(Long::parseLong).orElse(null),
                            attempt,
                            orderStatus.getCode(),
                            orderStatusDto.getDescription()
                    );

                    resultCanonical = new OrderCanonical();

                    resultCanonical.setEcommerceId(orderFulfillment.getEcommerceId());
                    resultCanonical.setStatusCode(orderStatus.getCode());
                    resultCanonical.setStatus(orderStatus.getType());
                    resultCanonical.setExternalId(Optional.ofNullable(externalId).map(Long::parseLong).orElse(null));
                    resultCanonical.setStatusDetail(orderStatusDto.getDescription());

                    break;

                default:
                    resultCanonical = new OrderCanonical();
                    resultCanonical.setStatusCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    resultCanonical.setStatus(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    break;

            }

            resultCanonical.setAttempt(attempt);
            resultCanonical.setAttemptTracker(attemptTracker);

        } else {
            resultCanonical = new OrderCanonical();
            resultCanonical.setStatusCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            resultCanonical.setStatus(Constant.OrderStatus.NOT_FOUND_ORDER.name());
        }

        return resultCanonical;

    }

}
