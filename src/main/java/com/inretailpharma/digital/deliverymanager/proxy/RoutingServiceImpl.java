package com.inretailpharma.digital.deliverymanager.proxy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.config.parameters.RoutingProperties;
import com.inretailpharma.digital.deliverymanager.dto.routing.CancelOrderDto;
import com.inretailpharma.digital.deliverymanager.dto.routing.RoutedOrderContainerDto;
import com.inretailpharma.digital.deliverymanager.dto.routing.TokenCredentialsDto;
import com.inretailpharma.digital.deliverymanager.dto.routing.TokenRequestDto;
import com.inretailpharma.digital.deliverymanager.dto.routing.TokenResponseDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.ObjectUtil;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Service("routing")
public class RoutingServiceImpl extends AbstractOrderService implements OrderExternalService {
	
	private ExternalServicesProperties externalServicesProperties;
	private RoutingProperties routingProperties;
	
	@Autowired
	public RoutingServiceImpl(ExternalServicesProperties externalServicesProperties,
			RoutingProperties routingProperties) {
		this.externalServicesProperties = externalServicesProperties;
		this.routingProperties = routingProperties;
	}
	
	
	@Override
	public Mono<OrderCanonical> createOrderRouting(Long ecommercePurchaseId, RoutedOrderContainerDto routedOrderContainerDto) {
		
		
		log.info("[START] RoutingService.createOrderRouting - order:{}", ObjectUtil.objectToJson(routedOrderContainerDto));

		log.info("[INFO] RoutingService.createOrderRouting url:{}",externalServicesProperties.getRoutingCreateOrderUri());
		
		TokenResponseDto token = getToken();		
		
		if (token != null && token.isSuccess() && token.getIdToken() != null ) {
			
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
					.header("Authorization", token.getIdToken())
					.bodyValue(routedOrderContainerDto)
					.exchange()
					.flatMap(r -> {
							
						if (r.statusCode().is2xxSuccessful()) {						
							
							return r.bodyToMono(String.class).flatMap(s -> {
								
								log.info("[END] RoutingService.createOrderRouting - order:{} - response{}", ecommercePurchaseId, s);
								return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.CONFIRMED_ROUTER, null));	
							});
						}
						
						return r.bodyToMono(String.class).flatMap(s -> {
							
							log.info("[ERROR] RoutingService.createOrderRouting - order:{} - code{} - response{}",
									ecommercePurchaseId, r.rawStatusCode(), s);					
							return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.CONFIRMED_ROUTER_ERROR, s));	
						});
	
					})
					.defaultIfEmpty(createResponse(ecommercePurchaseId, Constant.OrderStatus.CONFIRMED_ROUTER_ERROR, "EMPTY"))
					.onErrorResume(ex -> {
						ex.printStackTrace();
						log.error("[ERROR] RoutingService.createOrderRouting - order:{} - error:{}", ecommercePurchaseId, ex.getMessage());
						return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.CONFIRMED_ROUTER_ERROR, ex.getMessage()));
					});
			
		} else {
			
			return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.CONFIRMED_ROUTER_ERROR, "invalid token"));
			
		}


	}
	
	@Override
	public Mono<OrderCanonical> updateOrderRouting(Long ecommercePurchaseId) {
		
		log.info("[START] RoutingService.updateOrderRouting - order:{}", ecommercePurchaseId);

		log.info("[INFO] RoutingService.updateOrderRouting url:{}",externalServicesProperties.getRoutingCancelOrderUri());
		
		TokenResponseDto token = getToken();		
		
		if (token != null && token.isSuccess() && token.getIdToken() != null ) {
			
			CancelOrderDto dto = new CancelOrderDto(String.valueOf(ecommercePurchaseId));
			
			return WebClient
					.builder()
					.clientConnector(
							generateClientConnector(
									Integer.parseInt(externalServicesProperties.getRoutingCancelOrderConnectTimeout()),
									Long.parseLong(externalServicesProperties.getRoutingCancelOrderReadTimeout())
							)
					)
					.baseUrl(externalServicesProperties.getRoutingCancelOrderUri())
					.build()
					.put()
					.header("Authorization", token.getIdToken())
					.bodyValue(dto)
					.exchange()
					.flatMap(r -> {
							
						if (r.statusCode().is2xxSuccessful()) {						
							
							return r.bodyToMono(String.class).flatMap(s -> {
								
								log.info("[END] RoutingService.updateOrderRouting - order:{} - response{}", ecommercePurchaseId, s);
								return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.CANCEL_ORDER, null));	
							});
						}
						
						return r.bodyToMono(String.class).flatMap(s -> {
							
							log.info("[ERROR] RoutingService.updateOrderRouting - order:{} - code{} - response{}",
									ecommercePurchaseId, r.rawStatusCode(), s);					
							return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.ERROR_CANCELLED, s));	
						});
						
						
					})
					.defaultIfEmpty(createResponse(ecommercePurchaseId, Constant.OrderStatus.ERROR_CANCELLED, "EMPTY"))
					.onErrorResume(ex -> {
						ex.printStackTrace();
						log.error("[ERROR] RoutingService.updateOrderRouting - order:{} - error:{}", ecommercePurchaseId, ex.getMessage());
						return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.ERROR_CANCELLED, ex.getMessage()));
					});
			
		} else {
			
			return Mono.just(createResponse(ecommercePurchaseId, Constant.OrderStatus.ERROR_CANCELLED, "invalid token"));
			
		}
	}
	
	private Mono<TokenResponseDto> getTokenAsyn() {
		
		TokenCredentialsDto credentials = TokenCredentialsDto.builder()
				.username(routingProperties.getUsername())
				.password(routingProperties.getPassword())
				.build();
		
		TokenRequestDto request = TokenRequestDto.builder()
				.authParameters(credentials)
				.clientId(routingProperties.getClientId())
				.authFlow("USER_PASSWORD_AUTH")
				.build();
		
		//log.info("[START] router service - getToken - request:{}", ObjectUtil.objectToJson(request));
		log.info("url to getToken:{}",externalServicesProperties.getRoutingCreateTokenUri());
		
		return WebClient
				.builder()
				.clientConnector(
						generateClientConnector(
								Integer.parseInt(externalServicesProperties.getRoutingCreateTokenConnectTimeout()),
								Long.parseLong(externalServicesProperties.getRoutingCreateTokenReadTimeout())
						)
				)
				.baseUrl(externalServicesProperties.getRoutingCreateTokenUri())
				.build()
				.post()
				.header("Content-Type", "application/x-amz-json-1.1")
				.header("X-Amz-Target", "AWSCognitoIdentityProviderService.InitiateAuth")
				.bodyValue(request)
				.exchange()
				.flatMap(r -> {
						
					if (r.statusCode().is2xxSuccessful()) {						
						
						return r.bodyToMono(String.class).flatMap(response -> {
							
							TokenResponseDto r3 = new TokenResponseDto();
							r3.setSuccess(true);
							log.info("[END] router service - getToken - response{}", ObjectUtil.objectToJson(response));							
							return Mono.just(r3);
						});
					}	
					
					log.info("[ERROR] router service - getToken - code{}", r.rawStatusCode());					
					return Mono.just(new TokenResponseDto());	
				})
				.defaultIfEmpty(new TokenResponseDto())
				.onErrorResume(ex -> {
					ex.printStackTrace();
					log.error("[ERROR] router service - getToken - error:{}", ex.getMessage());
					return Mono.just(new TokenResponseDto());
				});

		
	}
	
	private TokenResponseDto getToken() {
		
		TokenCredentialsDto credentials = TokenCredentialsDto.builder()
				.username(routingProperties.getUsername())
				.password(routingProperties.getPassword())
				.build();
		
		TokenRequestDto requestBody = TokenRequestDto.builder()
				.authParameters(credentials)
				.clientId(routingProperties.getClientId())
				.authFlow("USER_PASSWORD_AUTH")
				.build();
	    
	    HttpClient httpClient = HttpClient.newHttpClient();	    
	    HttpRequest request = HttpRequest.newBuilder(URI.create(externalServicesProperties.getRoutingCreateTokenUri()))
	            .header("X-Amz-Target","AWSCognitoIdentityProviderService.InitiateAuth")
	            .header("Content-Type","application/x-amz-json-1.1")
	            .POST(HttpRequest.BodyPublishers.ofString(ObjectUtil.objectToJson(requestBody)))
	            .build();
	
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			
			if (response.statusCode() == 200) {
				
				log.info("[END] RoutingService.getToken - response{}", response.body());
				TokenResponseDto responseBody = ObjectUtil.jsonToObject(response.body(), TokenResponseDto.class);
				responseBody.setSuccess(true);
				return responseBody;
			}			

			log.error("[ERROR] RoutingService.getToken - code {}", response.statusCode());	
			
		} catch (Exception ex) {
			log.error("[ERROR] RoutingService.getToken - {}", ex.getMessage());
			ex.printStackTrace();
		}
		
		return new TokenResponseDto();
		
	}
	
	
	private OrderCanonical createResponse(Long ecommerceId, Constant.OrderStatus status, String detail) {		
		 return new OrderCanonical(ecommerceId, status.getCode(), status.getCode(), detail);
	}

}
