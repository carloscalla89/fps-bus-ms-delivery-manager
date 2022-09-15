package com.inretailpharma.digital.deliverymanager.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductDimensionDto {

	private String sku;
	private BigDecimal volume;
}
