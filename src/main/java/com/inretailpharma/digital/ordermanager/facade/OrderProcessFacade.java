package com.inretailpharma.digital.ordermanager.facade;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.canonical.management.OrderResultCanonical;
import com.inretailpharma.digital.ordermanager.entity.OrderFulfillment;
import com.inretailpharma.digital.ordermanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.ordermanager.entity.ServiceLocalOrderIdentity;
import com.inretailpharma.digital.ordermanager.proxy.OrderExternalService;
import com.inretailpharma.digital.ordermanager.transactions.OrderTransaction;
import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.events.KafkaEvent;
import com.inretailpharma.digital.ordermanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.ordermanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
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

    public OrderResultCanonical getUpdateOrder(OrderDto orderDto) {

        OrderFulfillmentCanonical orderFulfillment =
                objectToMapper
                        .convertIOrderDtoToOrderFulfillmentCanonical(
                                orderTransaction.getOrderByecommerceId(orderDto.getEcommercePurchaseId())
                        );

        OrderResultCanonical resultCanonical = orderExternalService
                .updateOrder(orderDto.getEcommercePurchaseId(), orderDto.getAction());

        switch (orderDto.getAction().getCode()) {
            case 1:
                Integer attempt = orderFulfillment.getAttempt() + 1;

                if (Optional
                        .ofNullable(resultCanonical.getStatus())
                        .orElse(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                        .equals(Constant.OrderStatus.FULFILLMENT_PROCESS_SUCCESS.getCode())) {

                    log.info("Update external id");

                    orderTransaction.updateExternalPurchaseId(
                            orderFulfillment.getTrackerCode(), resultCanonical.getExternalId()
                    );

                } else {
                    log.info("Update Reattmpt insink");
                    orderTransaction.updateReattemtpInsink(
                            orderFulfillment.getTrackerCode(), attempt,
                            resultCanonical.getStatus(), resultCanonical.getStatusDetail()
                    );
                }

                resultCanonical.setAttempt(attempt);

                break;
            case 2:
                break;

        }

        return resultCanonical;

    }

}
