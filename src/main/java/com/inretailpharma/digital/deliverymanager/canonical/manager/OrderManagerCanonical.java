package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderManagerCanonical {

    private Long ecommerceId;
    private Long trackerId;
    private Long externalId;
    private String statusCode;
    private String status;
    private String statusDescription;
    private String statusDetail;
    private Integer attempt;
    private Integer attemptTracker;
}
