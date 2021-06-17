package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("trackerAdapter")
@Slf4j
public class TrackerAdapter extends AdapterAbstractUtil implements ITrackerAdapter {

    private ApplicationContext context;

    @Autowired
    public TrackerAdapter(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Mono<OrderCanonical> createOrderToTracker(Class<?> classImplement, StoreCenterCanonical store,
                                                     Long ecommerceId, Long externalId, String statusName,
                                                     String orderCancelCode, String orderCancelDescription,
                                                     String orderCancelObservation, String statusDetail,
                                                     ActionDto actionDto) {

        OrderExternalService orderExternalService = ((OrderExternalService)context.getBean(classImplement));

        log.info("sendOrderTracker - ecommercePurchaseId:{}, ",ecommerceId);

        IOrderFulfillment iOrderFulfillment = getOrderByEcommerceId(ecommerceId);

        return orderExternalService
                .sendOrderToTracker(
                        iOrderFulfillment,
                        getItemsByOrderId(iOrderFulfillment.getOrderId()),
                        store,
                        externalId,
                        statusDetail,
                        statusName,
                        orderCancelCode,
                        orderCancelDescription,
                        orderCancelObservation
                        );
    }

    @Override
    public Mono<OrderCanonical> updateOrderToTracker(Class<?> classImplement, ActionDto actionDto, Long ecommerceId,
                                                     String company, String serviceType, String orderCancelDescription){

        OrderExternalService orderExternalService = ((OrderExternalService)context.getBean(classImplement));

        return orderExternalService
                .getResultfromExternalServices(ecommerceId, actionDto, company, serviceType, orderCancelDescription);

    }

}
