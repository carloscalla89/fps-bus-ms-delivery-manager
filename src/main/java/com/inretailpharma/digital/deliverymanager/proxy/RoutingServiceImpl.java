package com.inretailpharma.digital.deliverymanager.proxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.RoutedOrderContainerDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.ObjectUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Service("routing")
public class RoutingServiceImpl extends AbstractOrderService implements OrderExternalService {
	
	private ExternalServicesProperties externalServicesProperties;
	
	@Autowired
	public RoutingServiceImpl(ExternalServicesProperties externalServicesProperties) {
		this.externalServicesProperties = externalServicesProperties;
	}
	
	
	@Override
	public Mono<OrderCanonical> createOrderRouting(Long ecommercePurchaseId, RoutedOrderContainerDto routedOrderContainerDto) {
		
		
		log.info("[START] router service - order:{}", ObjectUtil.objectToJson(routedOrderContainerDto));

		log.info("url to create router:{}",externalServicesProperties.getRoutingCreateOrderUri());

		return WebClient
				.builder()
				.clientConnector(
						generateClientConnector(
								Integer.parseInt(externalServicesProperties.getRoutingCreateOrderConnectTimeout()),
								Long.parseLong(externalServicesProperties.getRoutingCreateOrderReadTimeout())
						)
				)
				.baseUrl(externalServicesProperties.getRoutingCreateOrderUri())
				.build()
				.post()
				.bodyValue(routedOrderContainerDto)
				.exchange()
				.flatMap(r -> {
						
					if (r.statusCode().is2xxSuccessful()) {						
						
						return r.bodyToMono(String.class).flatMap(s -> {
							
							log.info("[END] router service - order:{} - response{}", ecommercePurchaseId, s);
							return Mono.just(new OrderCanonical(ecommercePurchaseId, Constant.OrderStatus.CONFIRMED_TRACKER.getCode(), null));	
							
						});
					}
					
					log.info("[ERROR] router service - order:{} - code{}", ecommercePurchaseId, r.rawStatusCode());					
					return Mono.just(new OrderCanonical(ecommercePurchaseId, Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode(), null));	
				})
				.defaultIfEmpty(new OrderCanonical(ecommercePurchaseId, Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.getCode(), null))
				.onErrorResume(ex -> {
					ex.printStackTrace();
					log.error("[ERROR] router service - order:{} - error:{}", ecommercePurchaseId, ex.getMessage());
					return Mono.just(new OrderCanonical(ecommercePurchaseId, Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode(), null));
				});
	}

}
