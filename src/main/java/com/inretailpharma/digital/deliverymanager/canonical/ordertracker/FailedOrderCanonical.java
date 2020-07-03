package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FailedOrderCanonical implements Serializable {

	private Long orderId;
	private String reason;
}
