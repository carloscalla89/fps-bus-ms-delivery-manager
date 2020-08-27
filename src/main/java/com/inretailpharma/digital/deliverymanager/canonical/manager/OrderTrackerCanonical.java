package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderTrackerCanonical {

    private String inkaDeliveryId;
    private String cancelCode;
    private String cancelObservation;
    private String cancelReason;
    private String cancelClientReason;
    private String cancelAppType;
    private String userUpdate;

}
