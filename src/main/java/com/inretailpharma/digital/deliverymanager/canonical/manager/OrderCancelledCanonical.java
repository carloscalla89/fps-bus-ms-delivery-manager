package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.Data;

@Data
public class OrderCancelledCanonical {

    public OrderCancelledCanonical() {

    }

    public OrderCancelledCanonical(Long ecommerceId, String code, String name) {
        this.ecommerceId = ecommerceId;
        this.statusCode = code;
        this.statusName = name;
    }

    public OrderCancelledCanonical(Long ecommerceId, String code, String name, String detail) {
        this.ecommerceId = ecommerceId;

        this.statusCode = code;
        this.statusName = name;
        this.statusDetail = detail;
    }

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
