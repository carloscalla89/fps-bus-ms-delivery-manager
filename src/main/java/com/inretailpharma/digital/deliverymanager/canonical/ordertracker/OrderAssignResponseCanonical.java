package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderAssignResponseCanonical  implements Serializable {

	private String statusCode;
	private String detail;
    private List<Long> failedOrders;
}
