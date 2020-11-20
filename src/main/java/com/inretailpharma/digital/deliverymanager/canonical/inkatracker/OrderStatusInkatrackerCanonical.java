package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class OrderStatusInkatrackerCanonical {

    private String statusName;
    private Long statusDate;
    private String customNote;
    private String code;
    private String description;
    private Long cancelledDate;
}
