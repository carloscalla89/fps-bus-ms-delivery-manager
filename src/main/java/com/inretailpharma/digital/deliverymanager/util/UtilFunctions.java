package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
@Slf4j
public class UtilFunctions {

    public static ProcessFunctionInterface getSuccessResponseFunction =
            (y,z,e) -> {
                log.info("success response ecommerceId:{}, action:{}",y,z);
                OrderCanonical orderCanonical = new OrderCanonical();
                orderCanonical.setEcommerceId(y);

                Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(z).getOrderStatus();
                OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                orderStatusCanonical.setCode(orderStatus.getCode());
                orderStatusCanonical.setName(orderStatus.name());
                orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());

                orderCanonical.setOrderStatus(orderStatusCanonical);

                return Mono.just(orderCanonical);

            };

    public static ProcessFunctionInterface getErrorResponseFunction =
            (y,z,e) -> {

                OrderCanonical orderCanonical = new OrderCanonical();
                orderCanonical.setEcommerceId(y);

                Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(z).getOrderStatusError();
                OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                orderStatusCanonical.setCode(orderStatus.getCode());
                orderStatusCanonical.setName(orderStatus.name());
                orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());
                orderStatusCanonical.setDetail(e);

                orderCanonical.setOrderStatus(orderStatusCanonical);

                return Mono.just(orderCanonical);

            };

}
