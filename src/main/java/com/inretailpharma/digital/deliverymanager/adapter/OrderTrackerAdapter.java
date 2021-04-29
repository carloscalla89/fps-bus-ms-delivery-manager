package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderItemCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OrderTrackerAdapter extends AdapterAbstractUtil implements ITrackerAdapter {
    private ApplicationContext context;

    @Autowired
    public OrderTrackerAdapter(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Mono<OrderCanonical> createOrderToTracker(Class<?> classImplement, StoreCenterCanonical store,
                                                     Long ecommerceId, Long externalId, String statusName,
                                                     String orderCancelCode, String orderCancelDescription,
                                                     String orderCancelObservation, String statusDetail,
                                                     ActionDto actionDto) {

        OrderExternalService orderExternalService = ((OrderExternalService)context.getBean(classImplement));

        return orderExternalService
                .sendOrderToOrderTracker(getOrderFromIOrdersProjects(ecommerceId), actionDto);
    }

    @Override
    public Mono<OrderCanonical> updateOrderToTracker(Class<?> classImplement, ActionDto actionDto, Long ecommerceId,
                                                     String company, String serviceType, String orderCancelDescription) {

        OrderExternalService orderExternalService = ((OrderExternalService)context.getBean(classImplement));

        return orderExternalService.updateOrderStatus(ecommerceId, actionDto);
    }
}
