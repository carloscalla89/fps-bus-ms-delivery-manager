package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderDetailCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class UtilFunctions {

    public static ProcessFunctionInterface getSuccessResponseFunction =
            (y,z,e, x, i, s,c) -> {
                log.info("success response ecommerceId:{}, action:{}",y,z);
                OrderCanonical orderCanonical = new OrderCanonical();
                orderCanonical.setEcommerceId(y);
                orderCanonical.setId(i);

                OrderDetailCanonical orderDetailCanonical = new OrderDetailCanonical();
                orderDetailCanonical.setServiceType(s);

                orderCanonical.setOrderDetail(orderDetailCanonical);

                Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(z).getOrderStatus();
                OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                orderStatusCanonical.setCode(orderStatus.getCode());
                orderStatusCanonical.setName(orderStatus.name());
                orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());
                orderStatusCanonical.setFirstStatusName(x);
                orderStatusCanonical.setSuccessful(true);
                orderStatusCanonical.setCancellationCode(c);
                orderCanonical.setOrderStatus(orderStatusCanonical);

                return Mono.just(orderCanonical);

            };

    public static ProcessFunctionInterface getErrorResponseFunction =
            (y,z,e, x, i, s, c) -> {

                OrderCanonical orderCanonical = new OrderCanonical();
                orderCanonical.setEcommerceId(y);
                orderCanonical.setId(i);

                OrderDetailCanonical orderDetailCanonical = new OrderDetailCanonical();
                orderDetailCanonical.setServiceType(s);

                orderCanonical.setOrderDetail(orderDetailCanonical);

                Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(z).getOrderStatusError();
                OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                orderStatusCanonical.setCode(orderStatus.getCode());
                orderStatusCanonical.setName(orderStatus.name());
                orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());
                orderStatusCanonical.setDetail(e);
                orderStatusCanonical.setCancellationCode(c);
                orderCanonical.setOrderStatus(orderStatusCanonical);

                return Mono.just(orderCanonical);

            };



    public static LiquidationStatus processLiquidationStatus =
            (liquidationStatus, firstDigitalStatus,  action, cancelCode, serviceType) -> {

                StatusDto statusDto = new StatusDto();

                switch (action) {

                    case Constant.ACTION_DELIVER_ORDER:

                        if (serviceType.equalsIgnoreCase(Constant.DELIVERY)) {

                            statusDto.setCode(Constant.LIQUIDATION_STATUS_PENDING_CODE);
                            statusDto.setName(Constant.LIQUIDATION_STATUS_PENDING);

                        } else {

                            statusDto.setCode(Constant.LIQUIDATION_STATUS_BILLED_CODE);
                            statusDto.setName(Constant.LIQUIDATION_STATUS_BILLED);
                        }


                        break;

                    case Constant.ACTION_CANCEL_ORDER:
                    case Constant.ACTION_REJECT_ORDER:

                        if (firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED_TRACKER.name())
                                || firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.CHECKOUT_ORDER.name())
                                || firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.PICKED_ORDER.name())
                                || firstDigitalStatus.equalsIgnoreCase(Constant.OrderStatus.READY_PICKUP_ORDER.name())) {

                            statusDto.setCode(Constant.LIQUIDATION_STATUS_CANCELLED_CODE);
                            statusDto.setName(Constant.LIQUIDATION_STATUS_CANCELLED);

                        } else {

                            statusDto.setCode(Constant.LIQUIDATION_STATUS_PENDING_CODE);
                            statusDto.setName(Constant.LIQUIDATION_STATUS_PENDING);

                        }

                        break;
                }

                return statusDto;

            };
}