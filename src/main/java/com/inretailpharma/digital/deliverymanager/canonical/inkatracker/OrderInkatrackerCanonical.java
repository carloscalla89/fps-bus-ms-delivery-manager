package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

import java.util.List;

@Data
public class OrderInkatrackerCanonical {

    private Long orderExternalId;
    private OrderStatusInkatrackerCanonical orderStatus;
    private Long inkaDeliveryId;
    private List<InvoicedOrderCanonical> invoicedList;

}
