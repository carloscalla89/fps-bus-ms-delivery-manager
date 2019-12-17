package com.inretailpharma.digital.ordermanager.facade;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.canonical.OrderStatusErrorCanonical;
import com.inretailpharma.digital.ordermanager.entity.OrderFulfillment;
import com.inretailpharma.digital.ordermanager.transactions.OrderTransaction;
import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.events.KafkaEvent;
import com.inretailpharma.digital.ordermanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.ordermanager.util.Constant;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderProcessFacade {

    private OrderTransaction orderTransaction;
    private KafkaEvent kafkaEvent;
    private ObjectToMapper objectToMapper;

    public OrderProcessFacade(OrderTransaction orderTransaction, KafkaEvent kafkaEvent, ObjectToMapper objectToMapper) {
        this.orderTransaction = orderTransaction;
        this.kafkaEvent = kafkaEvent;
        this.objectToMapper = objectToMapper;
    }

    public OrderFulfillmentCanonical createOrder(OrderDto orderDto){

        OrderFulfillment orderFulfillment = orderTransaction
                .createOrder(objectToMapper.convertOrderdtoToOrderEntity(orderDto), orderDto);

        OrderFulfillmentCanonical orderFulfillmentCanonical = new OrderFulfillmentCanonical();
        orderFulfillmentCanonical.setTrackerCode(orderFulfillment.getId());

        return orderFulfillmentCanonical;

    }

    public List<OrderStatusErrorCanonical> getListOrdersByStatusError(){
        return orderTransaction
                .getListOrdersByStatus(
                        new HashSet<>(
                                Arrays.asList(
                                        Constant.orderStatus.ERROR_TRACKING_PROCESS,
                                        Constant.orderStatus.ERROR_BILLING_PROCESS,
                                        Constant.orderStatus.ERROR_ECOMMERCE_PROCESS)
                        )
                )
                .stream()
                .map(r -> objectToMapper.convertIOrderDtoToOrderCanonical(r))
                .collect(Collectors.toList());
    }

}