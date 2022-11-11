package com.inretailpharma.digital.deliverymanager.dto.routing;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenResponseDto {
	
	@JsonProperty("AuthenticationResult")
	private AuthenticationResult authenticationResult;	
	
	private boolean success = false;	
	
	public String getIdToken() {
		
		if (authenticationResult != null) {
			return authenticationResult.getIdToken();
		}
		return null;
	}
	
	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	private class AuthenticationResult {
		
		@JsonProperty("IdToken")	
		private String idToken;
	}

}
