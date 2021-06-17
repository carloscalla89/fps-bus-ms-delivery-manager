package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import reactor.core.publisher.Mono;

public interface ITrackerAdapter {

    Mono<OrderCanonical> createOrderToTracker(Class<?> classImplement, StoreCenterCanonical store,
                                              Long ecommerceId, Long externalId, String statusName,
                                              String orderCancelCode, String orderCancelDescription,
                                              String orderCancelObservation, String statusDetail, ActionDto actionDto);

    Mono<OrderCanonical> updateOrderToTracker(Class<?> classImplement, ActionDto actionDto, Long ecommerceId,
                                              String company, String serviceType, String orderCancelDescription);



    default Mono<OrderCanonical> evaluateTracker(Class<?> classImplement, ActionDto actionDto, StoreCenterCanonical store,
                                                 String companyCode, String serviceType, Long ecommerceId,
                                                 Long externalId, String statusName, String orderCancelCode,
                                                 String orderCancelDescription, String orderCancelObservation,
                                                 String statusDetail) {

        if ((Constant.ActionOrder.getByName(actionDto.getAction()).getMethod().equalsIgnoreCase(Constant.METHOD_CREATE))
            || classImplement.getSimpleName().equalsIgnoreCase("OrderTrackerServiceImpl")
                && actionDto.getAction().equalsIgnoreCase(Constant.ActionOrder.PREPARE_ORDER.name())) {

            return createOrderToTracker(classImplement, store, ecommerceId, externalId,
                    statusName, orderCancelCode, orderCancelDescription, orderCancelObservation, statusDetail, actionDto);

        } else {
            return updateOrderToTracker(classImplement, actionDto, ecommerceId, companyCode, serviceType, orderCancelDescription);
        }


    }
}
