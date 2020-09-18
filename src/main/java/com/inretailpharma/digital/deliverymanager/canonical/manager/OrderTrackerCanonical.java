package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.InvoicedOrderCanonical;
import lombok.Data;

import java.util.List;

@Data
public class OrderTrackerCanonical {

    private String inkaDeliveryId;
    private String cancelCode;
    private String cancelObservation;
    private String cancelReason;
    private String cancelClientReason;
    private String cancelAppType;
    private String userUpdate;
    private List<InvoicedOrderCanonical> invoicedList;

}
