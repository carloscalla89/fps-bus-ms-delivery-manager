package com.inretailpharma.digital.deliverymanager.adapter;


import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component("trackeradapter")
public class TrackerAdapterImpl extends AdapterAbstract implements AdapterInterface {

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService, Long ecommerceId,
                                                              ActionDto actionDto, String company, String serviceType,
                                                              Long id, String orderCancelCode, String orderCancelDescription,
                                                              String orderCancelObservation, String statusCode, String origin) {
        return orderExternalService
                .getResultfromExternalServices(ecommerceId, actionDto, company, serviceType, orderCancelDescription);
    }

    @Override
    public Mono<OrderCanonical> getOrder(IOrderFulfillment iOrderFulfillment) {
        return Mono
                .just(
                        objectToMapper
                                .getOrderFromIOrdersProjects(
                                        iOrderFulfillment,
                                        orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId())
                                )
                );



    }


}
