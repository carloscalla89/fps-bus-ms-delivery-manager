package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderDetailCanonical {

    // type of services
    private String serviceCode;
    private String serviceName;
    private String serviceType; // Constants of (DELIVERY) or PICKUP(PICKUP)

    // schedules
    private String confirmedSchedule;
    private String createdOrder;

    // attempts
    private Integer attempt;
    private Integer attemptTracker;

    //delivery time(lead time) and hours to delivery or pickup
    //of store if the serviceType is DELIVERY or PICKUP, respectively
    private Integer leadTime;
    private String startHour;
    private String endHour;

    // boolean if the order is programmed or not
    private boolean programmed;

}
