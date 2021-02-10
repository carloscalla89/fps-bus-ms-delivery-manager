package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component("ordertrackeradapter")
public class OrderTrackerAdapterImpl extends AdapterAbstract implements AdapterInterface{


    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService, Long ecommerceId,
                                                              ActionDto actionDto, String company, String serviceType,
                                                              Long id, String orderCancelCode,
                                                              String orderCancelObservation, String orderCancelAppType) {

        if (actionDto.getAction().equalsIgnoreCase(Constant.ActionOrder.PREPARE_ORDER.name())) {

            return createOrder(id, ecommerceId, actionDto, orderExternalService);

        } else {
            return orderExternalService.updateOrderStatus(ecommerceId, actionDto);
        }

    }


    private Mono<OrderCanonical> createOrder(Long id, Long ecommerceId, ActionDto actionDto,
                                             OrderExternalService orderExternalService) {

        log.info("[START] createOrder to Order-tracker - ecommerceId:{}, actionDto:{},", ecommerceId, actionDto);

        IOrderFulfillment orderDto = orderTransaction.getOrderByecommerceId(ecommerceId);
        OrderCanonical orderCanonical = objectToMapper.convertIOrderDtoToOrderFulfillmentCanonical(orderDto);

        List<IOrderItemFulfillment> orderItemDtoList = orderTransaction.getOrderItemByOrderFulfillmentId(orderDto.getOrderId());
        List<OrderItemCanonical> orderItemCanonicalList = orderItemDtoList.stream()
                .map(o -> objectToMapper.convertIOrderItemDtoToOrderItemFulfillmentCanonical(o, orderDto.getPartial()))
                .collect(Collectors.toList());

        orderCanonical.setOrderItems(orderItemCanonicalList);

        return orderExternalService
                .sendOrderToOrderTracker(orderCanonical, actionDto);


    }
}
