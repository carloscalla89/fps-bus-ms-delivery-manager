package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.adapter.OrderTrackerAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.TrackerAdapter;
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
    private String firstOrderStatusName;
    private boolean externalRouting;

    public UtilClass(String classImplementTracker) {
        this.classImplementTracker = classImplementTracker;
        this.externalRouting = false;
    }
    
    public UtilClass(String classImplementTracker, String serviceType, String actionName, String origin,
            String orderStatusName) {
		this.classImplementTracker = classImplementTracker;
		this.serviceType = serviceType;
		this.actionName = actionName;
		this.origin = origin;
		this.orderStatusName = orderStatusName;
		this.externalRouting = false;
	}    

    public UtilClass(String classImplementTracker, String serviceType, String actionName, String origin,
                     String orderStatusName, boolean externalRouting) {
        this.classImplementTracker = classImplementTracker;
        this.serviceType = serviceType;
        this.actionName = actionName;
        this.origin = origin;
        this.orderStatusName = orderStatusName;
        this.externalRouting = externalRouting;
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

    public String getFirstOrderStatusName() {
        return firstOrderStatusName;
    }

    public void setFirstOrderStatusName(String statusName) {
        firstOrderStatusName = statusName;
    }

    public List<Class<?>> getClassesToSend() {
        firstOrderStatusName = orderStatusName;
        if (serviceType.equalsIgnoreCase(Constant.PICKUP)) {
            return Collections.singletonList(TrackerAdapter.class);
        } else {
            List<Class<?>> classList = new ArrayList<>();
            switch (actionName) {
                case Constant.CHECKOUT_ORDER:
                    if (Constant.ORIGIN_DRUGSTORE_ENGINE.equalsIgnoreCase(origin)
                            && classImplementTracker.equalsIgnoreCase(Constant.TrackerImplementation.inkatracker.name())) {
                        classList.add(TrackerAdapter.class);
                    }
                    break;
                case Constant.PREPARE_ORDER:
                    if (!classImplementTracker.equalsIgnoreCase(Constant.TrackerImplementation.inkatracker.name())) {
                        classList.add(TrackerAdapter.class);
                    }
                    if (!Constant.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)
                            && !classImplementTracker.equalsIgnoreCase(Constant.TrackerImplementation.inkatracker.name())
                    		&& !externalRouting) {
                        classList.add(OrderTrackerAdapter.class);
                    }
                    break;
                case Constant.PICK_ORDER:
                case Constant.READY_TO_ASSIGN:
                case Constant.ASSIGN_ORDER:
                case Constant.ON_ROUTE_ORDER:
                case Constant.INVOICED_ORDER:
                    classList.add(TrackerAdapter.class);
                    break;
                case Constant.ARRIVAL_ORDER:
                    classList.add(TrackerAdapter.class);
                    if (Constant.ORIGIN_ROUTING.equalsIgnoreCase(origin)){
                        classList.add(OrderTrackerAdapter.class);
                    }
                    break;
                case Constant.DELIVER_ORDER:
                case Constant.CANCEL_ORDER:
                case Constant.REJECT_ORDER:
                    if (orderStatusName.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED_TRACKER.name())
                            || orderStatusName.equalsIgnoreCase(Constant.OrderStatus.CONFIRMED.name())
                            || orderStatusName.equalsIgnoreCase(Constant.OrderStatus.PICKED_ORDER.name())
                            || orderStatusName.equalsIgnoreCase(Constant.OrderStatus.CHECKOUT_ORDER.name())
                            || Constant.ORIGIN_OMNI_DELIVERY.equalsIgnoreCase(origin)
                            || Constant.ORIGIN_UNIFIED_POS.equalsIgnoreCase(Optional.ofNullable(origin).orElse(Constant.ORIGIN_UNIFIED_POS))) {
                        // aquí entra cuando la orden se entrega o rechaza desde el omnidelivery o posu
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
                        classList.add(OrderTrackerAdapter.class);
                    }
                    break;
            }
            return classList;
        }
    }

}
