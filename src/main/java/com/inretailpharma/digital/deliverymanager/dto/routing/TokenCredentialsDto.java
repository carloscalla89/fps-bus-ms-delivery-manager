package com.inretailpharma.digital.deliverymanager.dto.routing;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenCredentialsDto {
	
	@JsonProperty("USERNAME")	
	private String username;
	
	@JsonProperty("PASSWORD")	
	private String password;
}
