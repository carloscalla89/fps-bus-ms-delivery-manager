package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderDetailCanonical {

    // type of services
    private String serviceCode; // INKATRACKER_LITE_RAD, INKATRACKER_LITE_EXP, INKATRACKER_LITE_PROG....
    private String serviceShortCode; // RAD, RET, EXP, AM_PM, PROG
    private String serviceName; // inkatracker lite, inkatracker
    private String serviceType; // (DELIVERY) or PICKUP(PICKUP)
    private boolean serviceEnabled; // TRUE = ENABLED , FALSE = DISABLED
    private String serviceSourceChannel; // DIGITAL, CALL_CENTER
    private String serviceClassImplement; // inkatrackerlite, inkatracker

    // schedules
    private String confirmedSchedule;
    private String createdOrder;
    private String confirmedOrder;
    private String confirmedInsinkOrder;
    private String cancelledOrder;
    private String transactionVisaOrder;

    // attempts
    private Integer attempt;
    private Integer attemptTracker;

    //delivery time(lead time) in hours to delivery or pickup
    private Integer leadTime;
    private String startHour;
    private String endHour;
    private Integer daysToPickup;

    // boolean if the order is programmed or not
    private boolean programmed;

}
