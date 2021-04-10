package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.EcommerceMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service("deliveryDispatcherInka")
public class DeliveryDispatcherServiceImpl extends AbstractOrderService implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;
    private EcommerceMapper ecommerceMapper;

    public DeliveryDispatcherServiceImpl(ExternalServicesProperties externalServicesProperties,
                                         EcommerceMapper ecommerceMapper) {

        this.externalServicesProperties = externalServicesProperties;
        this.ecommerceMapper = ecommerceMapper;
    }


    @Override
    public Mono<OrderCanonical> sendOrderEcommerce(IOrderFulfillment iOrderFulfillment, List<IOrderItemFulfillment> itemFulfillments,
                                                   String action, StoreCenterCanonical storeCenterCanonical) {
        log.info("send order To sendOrderEcommerce");

        String dispatcherUri = externalServicesProperties.getDispatcherLegacySystemUri();

        log.info("url dispatcher new activated:{} - company:{}", dispatcherUri, iOrderFulfillment.getCompanyCode());

        return WebClient
                    .builder()
                    .clientConnector(
                            generateClientConnector(
                                    Integer.parseInt(externalServicesProperties.getDispatcherLegacySystemConnectTimeout()),
                                    Long.parseLong(externalServicesProperties.getDispatcherLegacySystemReadTimeout())
                            )
                    )
                    .baseUrl(dispatcherUri)
                    .build()
                    .post()
                    .body(Mono.just(ecommerceMapper.orderFulfillmentToOrderDto(iOrderFulfillment, itemFulfillments, storeCenterCanonical)),
                            OrderDto.class)
                    .exchange()
                    //.bodyToMono(ResponseDispatcherCanonical.class)
                    .flatMap(clientResponse -> mapResponseFromDispatcher(clientResponse,
                            iOrderFulfillment.getEcommerceId(), iOrderFulfillment.getCompanyCode())
                    )
                    .doOnSuccess(s -> log.info("Response is Success from dispatcher IKF"))
                    .defaultIfEmpty(
                            new OrderCanonical(
                                    iOrderFulfillment.getEcommerceId(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                    )
                    .doOnError(e -> {
                        e.printStackTrace();
                        log.error("Error from dispatcher:{}",e.getMessage());
                    })
                    .onErrorResume(e -> mapResponseErrorFromDispatcher(e, iOrderFulfillment.getEcommerceId()));

    }



    public Mono<com.inretailpharma.digital.deliverymanager.dto.OrderDto> getOrderFromEcommerce(Long ecommerceId) {
        log.info("[START] getOrderFromEcommerce inka:{}",ecommerceId);
        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getDispatcherOrderEcommerceConnectTimeout()),
                                Long.parseLong(externalServicesProperties.getDispatcherOrderEcommerceReadTimeout())
                        )
                )
                .baseUrl(externalServicesProperties.getDispatcherOrderEcommerceUri())
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
                    String errorMessage = "Error to invoking Delivery-dispatcher'"
                            + externalServicesProperties.getDispatcherOrderEcommerceUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);

                    return Mono.empty();
                });
    }
}
