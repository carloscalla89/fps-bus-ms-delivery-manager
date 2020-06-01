package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class OrderInkatrackerCanonical {

    private Long orderExternalId;
    private OrderStatusInkatrackerCanonical orderStatus;
    private Long inkaDeliveryId;
}
