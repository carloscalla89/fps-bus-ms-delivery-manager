package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderDetailCanonical {

    // type of services
    private String serviceCode;
    private String serviceName;
    private String serviceType; // (DELIVERY) or PICKUP(PICKUP)
    private boolean serviceEnabled; // TRUE = ENABLED , FALSE = DISABLED
    private String serviceSourceChannel; // APP, WEB, CALL_CENTER

    // schedules
    private String confirmedSchedule;
    private String createdOrder;

    // attempts
    private Integer attempt;
    private Integer attemptTracker;

    //delivery time(lead time) in hours to delivery or pickup
    private Integer leadTime;
    private String startHour;
    private String endHour;

    // boolean if the order is programmed or not
    private boolean programmed;

}
