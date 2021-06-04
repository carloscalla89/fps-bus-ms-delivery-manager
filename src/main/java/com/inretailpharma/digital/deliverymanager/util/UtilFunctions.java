package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class UtilFunctions {

    public static ProcessFunctionInterface getSuccessResponseFunction =
            (y,z,e, x) -> {
                log.info("success response ecommerceId:{}, action:{}",y,z);
                OrderCanonical orderCanonical = new OrderCanonical();
                orderCanonical.setEcommerceId(y);

                Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(z).getOrderStatus();
                OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                orderStatusCanonical.setCode(orderStatus.getCode());
                orderStatusCanonical.setName(orderStatus.name());
                orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());
                orderStatusCanonical.setFirstStatusName(x);
                orderCanonical.setOrderStatus(orderStatusCanonical);

                return Mono.just(orderCanonical);

            };

    public static ProcessFunctionInterface getErrorResponseFunction =
            (y,z,e, x) -> {

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



    public static LiquidationStatus processLiquidationStatus =
            (liquidationStatus, firstDigitalStatus,  action, cancelCode, serviceType) -> {

                String var = null;

                switch (action) {

                    case Constant.ACTION_DELIVER_ORDER:

                        if (serviceType.equalsIgnoreCase(Constant.DELIVERY)) {
                            var = Constant.LIQUIDATION_STATUS_PENDING;
                        } else {
                            var = Constant.LIQUIDATION_STATUS_BILLED;
                        }


                        break;

                    case Constant.ACTION_CANCEL_ORDER:
                    case Constant.ACTION_REJECT_ORDER:

                        if (firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED_TRACKER.name())
                                || firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.CHECKOUT_ORDER.name())
                                || firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.PICKED_ORDER.name())
                                || firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.READY_PICKUP_ORDER.name())) {
                            var = Constant.LIQUIDATION_STATUS_CANCELLED;
                        } else {
                            var = Constant.LIQUIDATION_STATUS_PENDING;
                        }

                        break;
                }

                return var;

            };
}
