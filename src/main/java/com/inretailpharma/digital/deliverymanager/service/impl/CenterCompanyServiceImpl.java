package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CenterCompanyCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class CenterCompanyServiceImpl implements CenterCompanyService {
	
	private ExternalServicesProperties externalServicesProperties;

	public CenterCompanyServiceImpl(ExternalServicesProperties externalServicesProperties) {
		this.externalServicesProperties = externalServicesProperties;
	}
	
	@Override
	public Mono<StoreCenterCanonical> getExternalInfo(String localCode) {
		
		log.info("[START] service to call api to CenterCompanyCanonical.getExternalInfo - uri:{} - body:{}",
                externalServicesProperties.getFulfillmentCenterGetCenterUri(), localCode);
		
		return WebClient
				.builder()
                .baseUrl(externalServicesProperties.getFulfillmentCenterGetCenterUri())
                .build()
                .get()
                .uri(builder ->
                		builder
                				.path("/{localCode}")
                                .build(localCode))
                .retrieve()
				.bodyToMono(StoreCenterCanonical.class)
				.doOnSuccess(r -> log.info("[END] service to call api to CenterCompanyCanonical.getExternalInfo - {}",r));

	}

	@Override
	public Mono<StoreCenterCanonical> getExternalInfo(String companyCode, String localCode) {
		log.info("[START] service to call api fulfillmentCenter- uri:{} - companyCode:{}, localCode:{}",
				externalServicesProperties.getFulfillmentCenterGetCenterUri(), companyCode, localCode);

		return WebClient
				.builder()
				.baseUrl(externalServicesProperties.getFulfillmentCenterGetCenterUri())
				.build()
				.get()
				.uri(builder ->
						builder
								.path("/{localCode}")
								.build(localCode)
				)
				.retrieve()
				.bodyToMono(StoreCenterCanonical.class)
				.defaultIfEmpty(new StoreCenterCanonical(localCode))
				.flatMap(r -> {
					r.setCompanyCode(companyCode);
					return Mono.just(r);
				})
				.doOnSuccess(r -> log.info("[END] service to call api to CenterCompanyCanonical.getExternalInfo - {}",r))
				.onErrorResume(r -> {

					r.printStackTrace();

					log.error("error in get info center store:{}",r.getMessage());

					StoreCenterCanonical storeCenterCanonical = new StoreCenterCanonical();
					storeCenterCanonical.setLocalCode(localCode);
					storeCenterCanonical.setCompanyCode(companyCode);

					return Mono.just(storeCenterCanonical);
				});

	}
}
