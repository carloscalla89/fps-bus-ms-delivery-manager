package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service("store")
public class CenterCompanyServiceImpl extends AbstractOrderService implements OrderExternalService {
	
	private ExternalServicesProperties externalServicesProperties;

	public CenterCompanyServiceImpl(ExternalServicesProperties externalServicesProperties) {
		this.externalServicesProperties = externalServicesProperties;
	}

	@Override
	public Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localCode) {
		log.info("[START] service to call api fulfillmentCenter- uri:{} - companyCode:{}, localCode:{}",
				externalServicesProperties.getFulfillmentCenterGetCenterUri(), companyCode, localCode);

		return WebClient
				.builder()
				.clientConnector(
						generateClientConnector(
								Integer.parseInt(externalServicesProperties.getFulfillmentCenterGetCenterConnectTimeOut()),
								Long.parseLong(externalServicesProperties.getFulfillmentCenterGetCenterReadTimeOut())
						)
				)
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
				.switchIfEmpty(Mono.defer(()-> {
					log.error("the response for uri:{} is empty",externalServicesProperties.getFulfillmentCenterGetCenterUri());
					return Mono.just(new StoreCenterCanonical(localCode));
				}))
				.flatMap(r -> {
					r.setCompanyCode(companyCode);
					return Mono.just(r);
				})
				.doOnSuccess(r -> log.info("[END] service to call api to fulfillmente-center  {}",r))
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
