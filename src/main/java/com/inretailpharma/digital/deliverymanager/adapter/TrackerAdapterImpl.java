package com.inretailpharma.digital.deliverymanager.adapter;


import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("trackeradapter")
public class TrackerAdapterImpl extends AdapterAbstract implements AdapterInterface {

    @Override
    public Mono<OrderCanonical> sendOrderTracker(OrderExternalService orderExternalService, StoreCenterCanonical storeCenter,
                                                 Long ecommercePurchaseId, Long externalId, String statusDetail,
                                                 String statusName, String orderCancelCode, String orderCancelObservation,
                                                 String orderCancelAppType) {

        log.info("sendOrderTracker - ecommercePurchaseId:{}, ",ecommercePurchaseId);
        IOrderFulfillment iOrderFulfillmentCase4 = this.getOrderTransaction().getOrderByecommerceId(ecommercePurchaseId);


        return orderExternalService
                    .sendOrderToTracker(
                            iOrderFulfillmentCase4,
                            orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillmentCase4.getOrderId()),
                            storeCenter,
                            externalId,
                            statusDetail,
                            statusName,
                            orderCancelCode,
                            orderCancelObservation
                    );


    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService, Long ecommerceId,
                                                              ActionDto actionDto, String company, String serviceType,
                                                              Long id, String orderCancelCode, String orderCancelObservation,
                                                              String orderCancelAppType, String statusCode, String origin) {
        return orderExternalService
                .getResultfromExternalServices(ecommerceId, actionDto, company, serviceType);
    }



}
