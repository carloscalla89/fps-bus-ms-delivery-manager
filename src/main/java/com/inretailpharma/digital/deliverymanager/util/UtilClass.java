package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.adapter.OrderTrackerAdapterImpl;
import com.inretailpharma.digital.deliverymanager.adapter.TrackerAdapterImpl;
import com.inretailpharma.digital.deliverymanager.proxy.InkatrackerLiteServiceImpl;
import com.inretailpharma.digital.deliverymanager.proxy.InkatrackerServiceImpl;
import com.inretailpharma.digital.deliverymanager.proxy.OrderTrackerServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class UtilClass {

    private String serviceTypeCode;
    private String serviceType;
    private String actionName;
    private String origin;
    private String orderStatus;

    public UtilClass(String serviceTypeCode) {
        this.serviceTypeCode = serviceTypeCode;
    }

    public UtilClass(String serviceTypeCode, String serviceType, String actionName, String origin, String orderStatus) {
        this.serviceTypeCode = serviceTypeCode;
        this.serviceType = serviceType;
        this.actionName = actionName;
        this.origin = origin;
        this.orderStatus = orderStatus;
    }

    public Class<?> getClassImplementationToOrderExternalService(Class<?> classType) {

        log.info("class implementation:{}", classType.getSimpleName());

        if (classType.getSimpleName().equalsIgnoreCase("TrackerAdapterImpl")) {
            return Constant.TrackerImplementation.getByCode(serviceTypeCode).getTrackerImplement();
        } else {
            return OrderTrackerServiceImpl.class;
        }

    }

    public Class<?> getClassToTracker() {

        return Constant.TrackerImplementation.getByCode(serviceTypeCode).getTrackerImplement();

    }
    /*

                        Si es (RET no lo envío al order-tracker o si el action es PICK_ORDER)
                            entonces no lo envío al order-tracker

                        SINO
                            Si el action es PREPARE
                            entonces lo envío al Order-tracker

                            Si el action es ASSIGN_ORDER
                            entonces no lo envío al order-tracker

                            Si el action es ARRIVE_ORDER
                            entonces no lo envío al order-tracker

                            Si el action es ON_ROUTE_ORDER
                            entonces no lo envío al order-tracker

                            Si el action es DELIVER_ORDER, REJECTED o CANCELLED_ORDER y el origin es OMNI_DELIVERY
                            entonces no lo envío al order-tracker

                            Si el action es CANCEL_ORDER o DELIVER_ORDER y no tiene origin,
                            entonces lo envío al order-tracker

                            Si el action es CANCEL_ORDER y el origin es APP o WEB,
                            entonces lo envío al order-tracker

                     */
    public List<Class<?>> getClassesToSend() {

        if (serviceType.equalsIgnoreCase(Constant.Constans.PICKUP)) {
            return Collections.singletonList(TrackerAdapterImpl.class);

        } else {

            List<Class<?>> classList = new ArrayList<>();


            switch (actionName) {

                case Constant.Constans.PREPARE_ORDER:
                    classList.add(TrackerAdapterImpl.class);
                    classList.add(OrderTrackerAdapterImpl.class);

                    break;

                case Constant.Constans.PICK_ORDER:
                case Constant.Constans.ASSIGN_ORDER:
                case Constant.Constans.ON_ROUTE_ORDER:
                case Constant.Constans.ARRIVAL_ORDER:

                    classList.add(TrackerAdapterImpl.class);

                    break;

                case Constant.Constans.DELIVER_ORDER:
                case Constant.Constans.CANCEL_ORDER:
                case Constant.Constans.REJECT_ORDER:

                    if (Constant.Constans.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)) {

                        classList.add(TrackerAdapterImpl.class);

                    } else if (Constant.Constans.ORIGIN_DIGITAL.equalsIgnoreCase(origin)) {

                        classList.add(OrderTrackerAdapterImpl.class);

                    } else if (orderStatus.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED_TRACKER.name())
                                || orderStatus.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED.name())
                                || orderStatus.equalsIgnoreCase(Constant.OrderStatus.PICKED_ORDER.name())){

                        classList.add(TrackerAdapterImpl.class);

                    } else {
                        classList.add(TrackerAdapterImpl.class);
                        classList.add(OrderTrackerAdapterImpl.class);
                    }

                    break;


            }

            return classList;

        }
    }

}
