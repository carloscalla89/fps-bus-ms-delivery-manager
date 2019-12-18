package com.inretailpharma.digital.ordermanager.facade;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
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

}
