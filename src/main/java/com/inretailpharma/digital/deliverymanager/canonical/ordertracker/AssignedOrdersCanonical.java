package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignedOrdersCanonical implements Serializable {
	
	private List<Long> createdOrders;
	private List<FailedOrderCanonical> failedOrders;
	private String assigmentSuccessful;
	private String message;
	
}
