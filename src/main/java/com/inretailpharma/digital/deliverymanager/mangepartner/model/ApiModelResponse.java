package com.inretailpharma.digital.deliverymanager.mangepartner.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiModelResponse {
	private String code;
	private String type;
	private String message;
}
