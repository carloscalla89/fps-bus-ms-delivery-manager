package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderAssignResponseCanonical {

	private String statusCode;
    private List<Long> failedOrders;
}
