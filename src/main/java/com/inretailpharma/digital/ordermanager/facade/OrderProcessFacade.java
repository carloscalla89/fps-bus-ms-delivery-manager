package com.inretailpharma.digital.ordermanager.facade;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.canonical.manager.OrderManagerCanonical;
import com.inretailpharma.digital.ordermanager.dto.OrderStatusDto;
import com.inretailpharma.digital.ordermanager.dto.ReservedOrderDto;
import com.inretailpharma.digital.ordermanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.ordermanager.proxy.OrderExternalService;
import com.inretailpharma.digital.ordermanager.transactions.OrderTransaction;
import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.events.KafkaEvent;
import com.inretailpharma.digital.ordermanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.ordermanager.util.Constant;
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

    public OrderManagerCanonical releaseReservedOrder(String ecommerceId, ReservedOrderDto reservedOrderDto) {

        return getUpdateOrder(
                Constant.ActionOrder.RELEASE_ORDER.name(),
                ecommerceId,
                reservedOrderDto.getExternalPurchaseId(),
                Optional.ofNullable(reservedOrderDto.getOrderStatusDto())
                        .map(OrderStatusDto::getDescription)
                        .orElse(null)
        );

    }

    public List<OrderFulfillmentCanonical> getListOrdersByStatusError(){
        return orderTransaction
                .getListOrdersByStatus(
                        new HashSet<>(
                                Arrays.asList(
                                        Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode(),
                                        Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode(),
                                        Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode())
                        )
                )
                .stream()
                .map(r -> objectToMapper.convertIOrderDtoToOrderFulfillmentCanonical(r))
                .collect(Collectors.toList());
    }

    public OrderManagerCanonical getUpdateOrder(String action, String ecommerceId, String externalId, String statusDetail) {
        log.info("[START] getUpdateOrder action:{}",action);


        Long ecommercePurchaseId = Long.parseLong(ecommerceId);
        OrderManagerCanonical resultCanonical;

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
                    // Case to release a order when this is reserved

                    log.info("Start to update reserved order");
                    Constant.OrderStatus status =  Optional.ofNullable(externalId)
                            .map(r -> Constant.OrderStatus.FULFILLMENT_PROCESS_SUCCESS)
                            .orElse(Constant.OrderStatus.ERROR_RELEASE_ORDER);

                    attempt = attempt + 1;

                    orderTransaction.updateReservedOrder(
                            orderFulfillment.getId(),
                            Optional.ofNullable(externalId).map(Long::parseLong).orElse(null),
                            attempt,
                            status.getCode(),
                            statusDetail
                    );

                    resultCanonical = new OrderManagerCanonical();

                    resultCanonical.setEcommerceId(orderFulfillment.getEcommerceId());
                    resultCanonical.setStatusCode(status.getCode());
                    resultCanonical.setStatus(status.name());
                    resultCanonical.setExternalId(Optional.ofNullable(externalId).map(Long::parseLong).orElse(null));
                    resultCanonical.setStatusDetail(statusDetail);

                    break;

                default:
                    resultCanonical = new OrderManagerCanonical();
                    resultCanonical.setStatusCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                    resultCanonical.setStatus(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                    break;

            }

            resultCanonical.setAttempt(attempt);
            resultCanonical.setAttemptTracker(attemptTracker);

        } else {
            resultCanonical = new OrderManagerCanonical();
            resultCanonical.setStatusCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            resultCanonical.setStatus(Constant.OrderStatus.NOT_FOUND_ORDER.name());
        }

        return resultCanonical;

    }

}
