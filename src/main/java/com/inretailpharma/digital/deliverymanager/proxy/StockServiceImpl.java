package com.inretailpharma.digital.deliverymanager.proxy;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service("stock")
public class StockServiceImpl extends AbstractOrderService  implements OrderExternalService{
	
	private final ExternalServicesProperties externalServicesProperties;

    public StockServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }
    
    @Override
	public Mono<Void> releaseStock(Long externalId) {
    
    	log.info("[START] service to call api legay-brigde to releaseStock - uri:{}, ecommerceId:{}",
    			externalServicesProperties.getLegacyBridgeReleaseStockUri(), externalId);
    	
        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getLegacyBridgeReleaseStockConnectTimeout()),
                                Long.parseLong(externalServicesProperties.getLegacyBridgeReleaseStockReadTimeout())
                        )
                )
                .baseUrl(externalServicesProperties.getLegacyBridgeReleaseStockUri())
                .build()
                .put()
                .uri(builder ->
                        builder
                                .path("/{externalId}")
                                .build(externalId))
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(String.class))
                .doOnSuccess((r) -> log.info("[END] service to call api legay-brigde to releaseStock - ecommerceId:{},{}",
                		externalId, r))
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Error to call legay-brigde releaseStock - ecommerceId:{} - empty", externalId);
                    return Mono.empty();
                }))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error to call legay-brigde releaseStock - ecommerceId:{} - error:{}", externalId, e.getMessage());
                    return Mono.empty();
                })                
                .then();
	}

}
