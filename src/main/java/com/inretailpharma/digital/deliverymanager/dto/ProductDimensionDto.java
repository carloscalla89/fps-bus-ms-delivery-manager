package com.inretailpharma.digital.deliverymanager.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductDimensionDto {

	private String codInka;
	private String description;
	private boolean fractionable;
	private BigDecimal volume;
	private int umv;
}
