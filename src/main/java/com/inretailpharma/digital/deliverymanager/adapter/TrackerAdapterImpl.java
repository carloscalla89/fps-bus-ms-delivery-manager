package com.inretailpharma.digital.deliverymanager.adapter;


import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("trackeradapter")
public class TrackerAdapterImpl extends AdapterAbstract implements AdapterInterface {

    @Autowired
    private CenterCompanyService centerCompanyService;

    @Override
    public Mono<OrderCanonical> sendOrderTracker(OrderExternalService orderExternalService, Long ecommercePurchaseId,
                                                 Long externalId, String statusDetail, String statusName,
                                                 String orderCancelCode, String orderCancelObservation,
                                                 String orderCancelAppType) {
        log.info("sendOrderTracker - ecommercePurchaseId:{}, ",ecommercePurchaseId);
        IOrderFulfillment iOrderFulfillmentCase4 = this.getOrderTransaction().getOrderByecommerceId(ecommercePurchaseId);

        return centerCompanyService
                .getExternalInfo(iOrderFulfillmentCase4.getCompanyCode(), iOrderFulfillmentCase4.getCenterCode())
                .flatMap(storeCenterCanonical -> {
                    // creando la orden a tracker CON EL ESTADO CANCELLED

                    return orderExternalService
                            .sendOrderToTracker(
                                    iOrderFulfillmentCase4,
                                    orderTransaction.getOrderItemByOrderFulfillmentId(ecommercePurchaseId),
                                    storeCenterCanonical,
                                    externalId,
                                    statusDetail,
                                    statusName,
                                    orderCancelCode,
                                    orderCancelObservation
                            );
                });

    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService, Long ecommerceId,
                                                              ActionDto actionDto, String company, String serviceType,
                                                              Long id, String orderCancelCode,
                                                              String orderCancelObservation, String orderCancelAppType) {
        return orderExternalService
                .getResultfromExternalServices(ecommerceId, actionDto, company, serviceType);
    }



}
