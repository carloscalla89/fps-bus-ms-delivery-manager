package com.inretailpharma.digital.deliverymanager.util;

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



    public static LiquidationStatus processLiquidationStatus =
            (digitalStatus) -> {

                String liquidationStatus;

                switch (digitalStatus) {

                    case Constant.CONFIRMED_STATUS:
                        liquidationStatus = Constant.LIQUIDATION_STATUS_1;
                        break;

                    case Constant.ERROR_INSERT_INKAVENTA_STATUS:
                    case Constant.ERROR_INSERT_TRACKER_STATUS:
                    case Constant.ORDER_FAILED_STATUS:
                        liquidationStatus = Constant.LIQUIDATION_STATUS_2;
                        break;

                    case Constant.CANCELLED_ORDER_STATUS:
                    case Constant.REJECTED_ORDER_STATUS:
                    case Constant.CANCELLED_ORDER_ONLINE_PAYMENT_STATUS:
                    case Constant.REJECTED_ORDER_ONLINE_PAYMENT_STATUS:
                    case Constant.CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK_STATUS:
                    case Constant.CANCELLED_ORDER_NOT_ENOUGH_STOCK_STATUS:

                        liquidationStatus = Constant.LIQUIDATION_STATUS_3;

                        break;

                    case Constant.DELIVERED_ORDER_STATUS:
                        liquidationStatus = Constant.LIQUIDATION_STATUS_4;
                        break;

                    default:
                        liquidationStatus = Constant.LIQUIDATION_STATUS_ERROR;
                }

                return liquidationStatus;

            };
}
