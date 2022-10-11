package com.inretailpharma.digital.deliverymanager.dto.routing;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenRequestDto implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1011408801930304032L;

	@JsonProperty("AuthParameters")	
	private TokenCredentialsDto authParameters;
	
	@JsonProperty("AuthFlow")	
	private String authFlow;
	
	@JsonProperty("ClientId")	
	private String clientId;	
}
