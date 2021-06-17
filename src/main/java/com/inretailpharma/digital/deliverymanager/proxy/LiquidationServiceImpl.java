package com.inretailpharma.digital.deliverymanager.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.LiquidationDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component("liquidation")
public class LiquidationServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    public LiquidationServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public Mono<OrderCanonical> createOrderToLiquidation(LiquidationDto liquidationDto) {

        try {
            log.info("[START] createOrderToLiquidation: uri:{}, dto:{}",
                    externalServicesProperties.getLiquidationCreateOrderUri(),new ObjectMapper().writeValueAsString(liquidationDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getLiquidationCreateOrderConnectTimeOut()),
                                Long.parseLong(externalServicesProperties.getLiquidationCreateOrderReadTimeOut())
                        )
                )
                .baseUrl(externalServicesProperties.getLiquidationCreateOrderUri())
                .build()
                .post()
                .body(Mono.just(liquidationDto), LiquidationDto.class)
                .exchange()
                .flatMap(clientResponse -> mapResponseFromTargetLiquidation(
                        clientResponse, Long.parseLong(liquidationDto.getEcommerceId()), liquidationDto.getStatus())
                )
                .doOnSuccess(s -> log.info("Response is Success in liquidation:{}",s))
                .switchIfEmpty(Mono.defer(() -> mapResponseFromTargetWithErrorOrEmpty(
                        Long.parseLong(liquidationDto.getEcommerceId()), liquidationDto.getStatus(), "La respuesta del servicio es vacía"))
                )
                .doOnError(e -> {
                    e.printStackTrace();
                    log.error("Error creating in us-liquidation:{}",e.getMessage());
                })
                .onErrorResume(e -> mapResponseFromTargetWithErrorOrEmpty(
                        Long.parseLong(liquidationDto.getEcommerceId()), liquidationDto.getStatus(), e.getMessage())
                );
    }

    @Override
    public Mono<OrderCanonical> updateOrderToLiquidation(String ecommerceId, StatusDto statusDto) {

        try {
            log.info("[START] updateOrderToLiquidation: uri:{},ecommerceId:{}, statusDto:{}",
                    externalServicesProperties.getLiquidationUpdateOrderUri(),ecommerceId,
                    new ObjectMapper().writeValueAsString(statusDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getLiquidationUpdateOrderConnectTimeOut()),
                                Long.parseLong(externalServicesProperties.getLiquidationUpdateOrderReadTimeOut())
                        )
                )
                .baseUrl(externalServicesProperties.getLiquidationUpdateOrderUri())
                .build()
                .patch()
                .uri(builder ->
                        builder
                                .path("/{ecommerceId}")
                                .build(ecommerceId))
                .body(Mono.just(statusDto), StatusDto.class)
                .exchange()
                .flatMap(clientResponse -> mapResponseFromTargetLiquidation(clientResponse, Long.parseLong(ecommerceId), statusDto)
                )
                .doOnSuccess(s -> log.info("Response is Success in update liquidation:{}",s))
                .switchIfEmpty(Mono.defer(() -> mapResponseFromTargetWithErrorOrEmpty(
                        Long.parseLong(ecommerceId), statusDto, "La respuesta al servicio es vacía"))
                )
                .doOnError(e -> {
                    e.printStackTrace();
                    log.error("Error updating in us-liquidation:{}",e.getMessage());
                })
                .onErrorResume(e -> mapResponseFromTargetWithErrorOrEmpty(
                        Long.parseLong(ecommerceId), statusDto, e.getMessage())
                );
    }

}
