package com.inretailpharma.digital.deliverymanager.client;

import com.inretailpharma.digital.deliverymanager.canonical.integration.ProductCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class ProductClient {

    @Autowired
    private ExternalServicesProperties externalServicesProperties;

    public List<ProductCanonical> getProducts(List<String> skus) {

        TcpClient tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, externalServicesProperties.getProductTimeout())
                .doOnConnected(connection -> connection.addHandlerLast(new ReadTimeoutHandler(externalServicesProperties.getProductTimeout())));

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(externalServicesProperties.getProductDetailsUri())
                .build()
                .get()
                .uri(uri ->uri.build(StringUtils.join(skus, ",")))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ProductCanonical>>() {})
                .onErrorResume(e -> {
                    log.error("get product details failed: {}", e.getMessage());
                    return Mono.just(Collections.emptyList());
                })
                .block();
    }
}
