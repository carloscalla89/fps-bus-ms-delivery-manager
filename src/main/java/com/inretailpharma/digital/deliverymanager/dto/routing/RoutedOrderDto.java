package com.inretailpharma.digital.deliverymanager.dto.routing;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class RoutedOrderDto {	
	
	@JsonProperty("pedido_id")	
	private String orderid;
	
	@JsonProperty("latitud")	
	private String latitude;
	
	@JsonProperty("longitud")	
	private String longitude;
	
	@JsonProperty("direccion")	
	private String address;
	
	@JsonProperty("tiempo_servicio")	
	private int deliveryTime;
	
	@JsonProperty("peso_entrega")	
	private int deliveryWeight;
	
	@JsonProperty("puntos_ventas_id")	
	private int localCode;
	
	@JsonProperty("unidad_medida_id")	
	private int measurementUnit;
	
	@JsonProperty("tiempo_solicitud")	
	private String creationDate;
	
	@JsonProperty("prioridad_entrega")	
	private int priority;

	@JsonProperty("hora_inicio_promesa")	
	private String scheduledTimeStart;
	
	@JsonProperty("hora_fin_promesa")	
	private String scheduledTimeEnd;

}
