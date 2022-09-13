package com.inretailpharma.digital.deliverymanager.dto.routing;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class CancelOrderDto {
	
	@JsonProperty("cancelarpedido")	
	private String orderId;
	
	public CancelOrderDto(String orderId) {
		this.orderId = orderId;
	}
}
