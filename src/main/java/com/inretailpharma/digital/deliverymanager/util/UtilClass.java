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

    private String classImplementTracker;
    private String serviceType;
    private String actionName;
    private String origin;
    private String orderStatus;
    private boolean sendNewFlow;

    public UtilClass(String classImplementTracker) {
        this.classImplementTracker = classImplementTracker;
    }

    public UtilClass(String classImplementTracker, String serviceType, String actionName, String origin, String orderStatus,
                     boolean sendNewFlow) {
        this.classImplementTracker = classImplementTracker;
        this.serviceType = serviceType;
        this.actionName = actionName;
        this.origin = origin;
        this.orderStatus = orderStatus;
        this.sendNewFlow = sendNewFlow;
    }

    public Class<?> getClassImplementationToOrderExternalService(Class<?> classType) {

        log.info("class implementation:{}", classType.getSimpleName());

        if (classType.getSimpleName().equalsIgnoreCase("TrackerAdapterImpl")) {
            return Constant.TrackerImplementation.getIdByClassImplement(classImplementTracker).getTrackerImplement();
        } else {
            return OrderTrackerServiceImpl.class;
        }

    }

    public Class<?> getClassToTracker() {

        return Constant.TrackerImplementation.getIdByClassImplement(classImplementTracker).getTrackerImplement();

    }

    public List<Class<?>> getClassesToSend() {

        if (serviceType.equalsIgnoreCase(Constant.PICKUP)) {
            return Collections.singletonList(TrackerAdapterImpl.class);

        } else {

            List<Class<?>> classList = new ArrayList<>();


            switch (actionName) {

                case Constant.PREPARE_ORDER:
                    classList.add(TrackerAdapterImpl.class);

                    if (sendNewFlow) {
                        classList.add(OrderTrackerAdapterImpl.class);
                    }

                    break;

                case Constant.PICK_ORDER:
                case Constant.ASSIGN_ORDER:
                case Constant.ON_ROUTE_ORDER:
                case Constant.ARRIVAL_ORDER:

                    classList.add(TrackerAdapterImpl.class);

                    break;

                case Constant.DELIVER_ORDER:
                case Constant.CANCEL_ORDER:
                case Constant.REJECT_ORDER:

                    if (Constant.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)) {

                        classList.add(TrackerAdapterImpl.class);

                    } else if (Constant.ORIGIN_DIGITAL.equalsIgnoreCase(origin)) {

                        classList.add(OrderTrackerAdapterImpl.class);

                    } else if (orderStatus.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED_TRACKER.name())
                                || orderStatus.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED.name())
                                || orderStatus.equalsIgnoreCase(Constant.OrderStatus.PICKED_ORDER.name())){

                        classList.add(TrackerAdapterImpl.class);

                    } else {
                        classList.add(TrackerAdapterImpl.class);

                        if (sendNewFlow) {
                            classList.add(OrderTrackerAdapterImpl.class);
                        }
                    }

                    break;


            }

            return classList;

        }
    }

}
