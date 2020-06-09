package com.inretailpharma.digital.deliverymanager.service.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CenterCompanyCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CenterCompanyServiceImpl implements CenterCompanyService {
	
	private ExternalServicesProperties externalServicesProperties;

	public CenterCompanyServiceImpl(ExternalServicesProperties externalServicesProperties) {
		this.externalServicesProperties = externalServicesProperties;
	}
	
	@Override
	public CenterCompanyCanonical getExternalInfo(String localCode) {		
		
		log.info("[START] service to call api to CenterCompanyCanonical.getExternalInfo - uri:{} - body:{}",
                externalServicesProperties.getFulfillmentCenterGetCenterUri(), localCode);
		
		ResponseEntity<CenterCompanyCanonical> response = WebClient
				.builder()
                .baseUrl(externalServicesProperties.getFulfillmentCenterGetCenterUri())
                .build()
                .get()
                .uri(builder ->
                		builder
                				.path("/{localCode}")
                                .build(localCode))
                .retrieve()
                .toEntity(CenterCompanyCanonical.class)
            	.block();
					
		
		log.info("[END] service to call api to CenterCompanyCanonical.getExternalInfo - s:{}", response.getBody());
		return response.getBody();
	}
}
