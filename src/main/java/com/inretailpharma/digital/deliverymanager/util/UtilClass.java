package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.adapter.OrderTrackerAdapterImpl;
import com.inretailpharma.digital.deliverymanager.adapter.TrackerAdapterImpl;
import com.inretailpharma.digital.deliverymanager.proxy.OrderTrackerServiceImpl;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

        return Constant.TrackerImplementation.getIdByClassImplement(classImplementTracker).getTargetName();

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

                    if (sendNewFlow && !Constant.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)) {
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

                    if (orderStatusName.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED_TRACKER.name())
                            || orderStatusName.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED.name())
                            || orderStatusName.equalsIgnoreCase(Constant.OrderStatus.PICKED_ORDER.name())
                            || Constant.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)) {

                        // aquí entra cuando la orden se entrega o rechaza desde el omnidelivery
                        // se cancela desde el pos unificado y la orden no se ha pickeado aún

                        classList.add(TrackerAdapterImpl.class);

                    } else if (Constant.ORIGIN_FARMADASHBOARD.equalsIgnoreCase(origin)){
                        // aqui entra cuando la orden es cancelada, rechazada o entregada desde el farmadashboard
                        classList.add(OrderTrackerAdapterImpl.class);
                    } else {
                        // casos:
                        // cuando la orden se cancela desde la web o app
                        // cuando se pone como entregada o cancelado desde el pos
                        // cuando se rechaza desde el inkatracker web
                        // cuando lo ejecuta el módulo de BBR
                        classList.add(TrackerAdapterImpl.class);

                        if (sendNewFlow || Constant.ORIGIN_BBR.equalsIgnoreCase(origin)) {
                            classList.add(OrderTrackerAdapterImpl.class);
                        }
                    }

                    break;
            }

            return classList;

        }
    }

}
