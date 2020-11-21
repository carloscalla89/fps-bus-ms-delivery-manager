package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class OrderStatusInkatrackerCanonical {

    // ambos
    private String code;
    private String description;
    private String statusName;

    // inkatracker cuando se cancela
    private Long statusDate;
    private String customNote;

    // inkatrackerlite cuando se cancela
    private Long cancelDate;
    private String cancelReasonCode;
    private String cancelMessageNote;


    // pendiente por asignar
    private Long cancelledDate;
}
