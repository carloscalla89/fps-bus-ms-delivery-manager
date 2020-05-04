package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderTrackerCanonical {

    // canonical status
    private Long ecommerceId;
    private OrderStatusCanonical orderStatus;
}
