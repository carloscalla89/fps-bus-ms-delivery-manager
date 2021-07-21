package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class InsinkAdapter extends AdapterAbstractUtil implements IInsinkAdapter {

    @Override
    public Mono<OrderDto> getOrderEcommerce(Long ecommerceId) {
        log.info("[START] getOrderEcommerce from insink call center inka:{} with uri:{}"
                ,ecommerceId, uriGetInsinkCallCenter());

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(connectTimeOutGetInsinkCallCenter()),
                                Long.parseLong(readTimeOutGetInsinkCallCenter())
                        )
                )
                .baseUrl(uriGetInsinkCallCenter())
                .build()
                .get()
                .uri(builder ->
                        builder
                                .path("/{orderId}")
                                .build(ecommerceId))
                .exchange()
                .flatMap(clientResponse -> {

                    if (clientResponse.statusCode().is2xxSuccessful()) {
                        return clientResponse
                                .bodyToMono(com.inretailpharma.digital.deliverymanager.dto.OrderDto.class);
                    }
                    return Mono.error(new CustomException("Error to get order from insink call center",
                            clientResponse.statusCode().value()));

                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    String errorMessage = "Error to invoking Delivery-dispatcher'" + uriGetInsinkCallCenter() +
                            "':" + e.getMessage();
                    log.error(errorMessage);

                    return Mono.empty();
                });
    }
}
