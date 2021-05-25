package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.adapter.OrderTrackerAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.TrackerAdapter;
import com.inretailpharma.digital.deliverymanager.proxy.OrderTrackerServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class UtilClass {

    private String classImplementTracker;
    private String serviceType;
    private String actionName;
    private String origin;
    private String target;
    private String orderStatusName;
    private boolean sendNewFlow;

    public UtilClass(String classImplementTracker) {
        this.classImplementTracker = classImplementTracker;
    }

    public UtilClass(String classImplementTracker, String serviceType, String actionName, String origin, String orderStatusName,
                     boolean sendNewFlow) {
        this.classImplementTracker = classImplementTracker;
        this.serviceType = serviceType;
        this.actionName = actionName;
        this.origin = origin;
        this.orderStatusName = orderStatusName;
        this.sendNewFlow = sendNewFlow;
    }

    public String getOnlyTargetComponentTracker() {

        return Constant.TrackerImplementation.getClassImplement(classImplementTracker).getTargetName();

    }

    public Class<?> getClassImplementationToOrderExternalService(Class<?> classType) {

        log.info("class implementation:{}", classType.getSimpleName());

        if (classType.getSimpleName().equalsIgnoreCase("trackerAdapter")) {

            return Constant.TrackerImplementation.getClassImplement(classImplementTracker).getTrackerImplement();

        } else {
            return OrderTrackerServiceImpl.class;
        }

    }

    public Class<?> getClassToTracker() {

        return Constant.TrackerImplementation.getClassImplement(classImplementTracker).getTrackerImplement();

    }

    public List<Class<?>> getClassesToSend() {

        if (serviceType.equalsIgnoreCase(Constant.PICKUP)) {
            return Collections.singletonList(TrackerAdapter.class);

        } else {

            List<Class<?>> classList = new ArrayList<>();


            switch (actionName) {

                case Constant.PREPARE_ORDER:
                    classList.add(TrackerAdapter.class);

                    if (!Constant.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)
                            && classImplementTracker.equalsIgnoreCase(Constant.TrackerImplementation.inkatrackerlite.name())) {
                        classList.add(OrderTrackerAdapter.class);
                    }

                    break;

                case Constant.PICK_ORDER:
                case Constant.ASSIGN_ORDER:
                case Constant.ON_ROUTE_ORDER:
                case Constant.ARRIVAL_ORDER:
                case Constant.INVOICED_ORDER:

                    classList.add(TrackerAdapter.class);

                    break;

                case Constant.DELIVER_ORDER:
                case Constant.CANCEL_ORDER:
                case Constant.REJECT_ORDER:

                    if (orderStatusName.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED_TRACKER.name())
                            || orderStatusName.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED.name())
                            || orderStatusName.equalsIgnoreCase(Constant.OrderStatus.PICKED_ORDER.name())
                            || Constant.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)) {

                        // aquí entra cuando la orden se entrega o rechaza desde el omnidelivery
                        // se cancela desde el pos unificado y la orden no se ha pickeado aún

                        classList.add(TrackerAdapter.class);

                    } else {
                        // casos:
                        // cuando la orden es cancelada desde el farmadashboard
                        // cuando la orden se cancela desde la web o app
                        // cuando se pone como entregada o cancelado desde el pos
                        // cuando se rechaza desde el inkatracker web
                        // cuando lo ejecuta el módulo de BBR

                        classList.add(TrackerAdapter.class);

                        if (!(orderStatusName.equalsIgnoreCase(Constant.OrderStatus.PREPARED_ORDER.name())
                                && classImplementTracker.equalsIgnoreCase(Constant.TrackerImplementation.inkatracker.name()))) {
                            classList.add(OrderTrackerAdapter.class);
                        }

                    }

                    break;
            }

            return classList;

        }
    }

}
