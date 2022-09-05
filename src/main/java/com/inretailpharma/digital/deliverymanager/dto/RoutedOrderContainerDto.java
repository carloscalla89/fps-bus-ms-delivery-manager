package com.inretailpharma.digital.deliverymanager.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RoutedOrderContainerDto {
	
	@JsonProperty("pedidos")	
	private List<RoutedOrderDto> orders;
}
