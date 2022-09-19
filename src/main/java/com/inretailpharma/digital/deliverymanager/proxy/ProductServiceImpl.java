package com.inretailpharma.digital.deliverymanager.proxy;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ProductDimensionDto;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

@Slf4j
@Component
public class ProductServiceImpl implements ProductService {
	
	private ExternalServicesProperties externalServicesProperties;
	
	@Autowired
	public ProductServiceImpl(ExternalServicesProperties externalServicesProperties) {
		this.externalServicesProperties = externalServicesProperties;
	}
	
	@Override
	public Flux<ProductDimensionDto> getDimensions(List<String> productCodes) {
		
		String productCodesArray = productCodes.stream().collect(Collectors.joining(","));	
		
		log.info("[START] ProductService.getDimensions - productCodes: {}", productCodesArray);

		log.info("[INFO] ProductService.getDimensions url:{}", externalServicesProperties.getProductGetDimensionsUri());
		
			
		
		return WebClient
				.builder()
				.clientConnector(
						generateClientConnector(
								Integer.parseInt(externalServicesProperties.getProductGetDimensionsConnectTimeout()),
								Long.parseLong(externalServicesProperties.getProductGetDimensionsReadTimeout())
						)
				)
				.baseUrl(externalServicesProperties.getProductGetDimensionsUri())
				.build()
				.get()
				.uri(builder ->
						builder
								.path("/{productCodes}")
								.build(productCodesArray)
				)
				.retrieve()
				.bodyToFlux(ProductDimensionDto.class)
				.onErrorResume(r -> {
					
					log.error("[ERROR] ProductService.getDimensions {}", r.getMessage());
					r.printStackTrace();
					
					ProductDimensionDto dto = new ProductDimensionDto();
					dto.setCodInka("0");
					dto.setFractionable(false);
					dto.setVolume(BigDecimal.TEN);
					return Flux.just(dto);
				});

	}	
	
	private ClientHttpConnector generateClientConnector(int connectionTimeOut, long readTimeOut) {
		log.info("generateClientConnector, connectionTimeOut:{}, readTimeOut:{}",connectionTimeOut,readTimeOut);
		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(tcpClient -> {
					tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeOut);
					tcpClient = tcpClient.doOnConnected(conn -> conn
							.addHandlerLast(
									new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS))
					);
					return tcpClient;
				});



		return new ReactorClientHttpConnector(httpClient);

	}

		
}
