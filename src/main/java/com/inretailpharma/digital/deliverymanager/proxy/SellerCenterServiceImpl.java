package com.inretailpharma.digital.deliverymanager.proxy;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service("sellerCenterService")
public class SellerCenterServiceImpl extends AbstractOrderService implements OrderExternalService {
	private ExternalServicesProperties externalServicesProperties;

    public SellerCenterServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }
	
	@Override
	public Mono<String> addControversy(ControversyRequestDto controversyRequestDto, Long ecommerceId) {
		String uri = externalServicesProperties.getAddControversyUri() + ecommerceId + "/controversies";
		
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
}
