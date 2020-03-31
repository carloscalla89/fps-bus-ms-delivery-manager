package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderCancelledCanonical {

    private Long ecommerceId;
    private Long externalId;

    // Canonical local and company
    private String localCode;
    private String local;
    private String company;

    // type of services
    private String serviceCode;
    private String serviceName;
    private String serviceType; // Constants of (DELIVERY) or PICKUP(PICKUP)

    // schedule
    private String confirmedSchedule;

    // Status
    private String statusCode;
    private String statusName;
    private String statusDetail;
}
