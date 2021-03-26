package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceOffered{

	private String code;
	private String service;
	private String shortName;
	private String startHour; // hora de inicio del servicio en la botica
	private String endHour; // hora de inicio del servicio
	private boolean enabled;

}
