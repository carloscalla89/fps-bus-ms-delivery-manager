package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import lombok.Data;

@Data
public class OrderToAssignCanonical {

	private Integer serviceType;
	private Long orderId;
}
