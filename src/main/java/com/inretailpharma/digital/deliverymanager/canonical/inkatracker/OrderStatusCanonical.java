package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class OrderStatusCanonical {

    private String statusName;
    private Long statusDate;
    private String statusNote;
    private Double latitude;
    private Double longitude;
    private String updatedBy;
    private String optionCode;
    private String customNote;
    private String code;
    private String payBackEnvelope;
    private ShoppingCartStatusReasonCanonical reason;
}
