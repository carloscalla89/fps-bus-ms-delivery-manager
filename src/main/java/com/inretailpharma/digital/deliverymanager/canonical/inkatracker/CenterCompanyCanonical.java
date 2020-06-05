package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CenterCompanyCanonical  implements Serializable {

	private String localCode;
	private String name;
	private BigDecimal latitude;
	private BigDecimal longitude;	
}
