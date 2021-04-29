package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component("dispatcher")
public class DeliveryDispatcherAdapter extends AdapterAbstractUtil implements IDeliveryDispatcherAdapter {

    @Override
    public Mono<OrderCanonical> sendRetryInsink(Long ecommerceId, String companyCode, StoreCenterCanonical store) {

        String uri = companyCode.equalsIgnoreCase(Constant.COMPANY_CODE_IFK) ?  uriRetryDDinka() : uriRetryDDmifa();

        log.info("[START] Send retry Insink FROM DD- uri:{}, company:{}", uri, companyCode);

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(connectTimeOutRetryDD()),
                                Long.parseLong(readTimeOutRetryDD())
                        )
                )
                .baseUrl(uri)
                .build()
                .post()
                .body(Mono.just(getMappOrder(ecommerceId, store)), OrderDto.class)
                .exchange()
                .flatMap(clientResponse -> mapResponseFromDispatcher(clientResponse, ecommerceId, companyCode))
                .doOnSuccess(s -> log.info("Response is Success from dispatcher IKF"))
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                )
                .doOnError(e -> {
                    e.printStackTrace();
                    log.error("Error from dispatcher:{}",e.getMessage());
                })
                .onErrorResume(e -> mapResponseErrorFromDispatcher(e, ecommerceId));
    }

    @Override
    public Mono<OrderDto> getOrderEcommerce(Long ecommerceId, String companyCode) {
        log.info("[START] getOrderFromEcommerce inka:{}",ecommerceId);

        String uri = companyCode.equalsIgnoreCase(Constant.COMPANY_CODE_IFK) ? uriGetFillinka() : uriGetFillmifa();

        log.info("[START] get fill order FROM DD- uri:{}, company:{}", uri, companyCode);

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(connectTimeOutGetFillDD()),
                                Long.parseLong(readTimeOutGetFillDD())
                        )
                )
                .baseUrl(uri)
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
                    return Mono.error(new CustomException("Error to get order from ecommerce",clientResponse.statusCode().value()));

                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    String errorMessage = "Error to invoking Delivery-dispatcher'" + uri + "':" + e.getMessage();
                    log.error(errorMessage);

                    return Mono.empty();
                });
    }
}
