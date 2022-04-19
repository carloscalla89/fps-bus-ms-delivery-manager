package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import lombok.Data;

import java.util.List;

@Data
public class OrdersSelectedResponse {
    private List<OrderCanonicalFulfitment> orders;
}
