package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalSellerCenterProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service("sellerCenterService")
public class SellerCenterServiceImpl extends AbstractOrderService implements OrderExternalService {

	private ExternalSellerCenterProperties externalServicesProperties;

    public SellerCenterServiceImpl(ExternalSellerCenterProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }
	
	@Override
	public Mono<String> addControversy(ControversyRequestDto controversyRequestDto, Long ecommerceId) {
		String uri = externalServicesProperties.getHost() + externalServicesProperties.getServicesAddControversyUri() + ecommerceId + "/controversies";
		
		log.info("[START] call to SellerCenter - add controversy - uri:{} - body:{}", uri, controversyRequestDto);
    	
    	return WebClient
            	.create(uri)
            	.post()
            	.bodyValue(controversyRequestDto)
            	.retrieve()
            	.bodyToMono(String.class)
            	.doOnSuccess(response -> log.info("[END] call to SellerCenter - add controversy - response {}", response))
	        	.defaultIfEmpty("Empty response")
	        	.onErrorResume(ex -> {
                    log.error("[ERROR] call to SellerCenter - add controversy", ex);
                    return Mono.just(ex.getMessage());
                });
	}

	@Override
	public Mono<Void> updateStatusOrderSeller(Long externalId, String status) {
		String uri = externalServicesProperties.getHost()
						+ externalServicesProperties
							.getServicesUpdateStatusUri()
							.replace("{ecommerceId}",externalId.toString())
							.replace("{statusCode}",status);

		log.info("updateStatusOrderSeller uri seller center:{}", uri);

		return WebClient
				.create(uri)
				.patch()
				.exchange()
				.doOnSuccess(response -> log.info("[END] call to SellerCenter - updateStatusOrderSeller - response {}", response.statusCode()))
				.subscribeOn(Schedulers.parallel())
				.doOnError(e -> {
					e.printStackTrace();
					log.error("Error to Call seller with ecommerceId:{} and error:{}",
							externalId,e.getMessage());
				})
				.doOnSuccess((r) -> log.info("[END] service to Call seller with ecommerceId:{},{}", externalId,r))
				.then();
	}
}
