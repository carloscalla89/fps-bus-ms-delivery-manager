package com.inretailpharma.digital.deliverymanager.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service("inkatrackerlite")
public class InkatrackerLiteServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;
    private ObjectToMapper objectToMapper;

    public InkatrackerLiteServiceImpl(ExternalServicesProperties externalServicesProperties, ObjectToMapper objectToMapper) {
        this.externalServicesProperties = externalServicesProperties;
        this.objectToMapper = objectToMapper;
    }


    @Override
    public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                                   List<IOrderItemFulfillment> itemFulfillments,
                                                   StoreCenterCanonical storeCenterCanonical,
                                                   Long externalId, String statusDetail) {

        return Mono
                .just(objectToMapper.convertOrderToOrderInkatrackerCanonical(iOrderFulfillment, itemFulfillments,
                        storeCenterCanonical, externalId, Constant.OrderStatus.CONFIRMED_TRACKER.name())
                )
                .flatMap(b -> {

                    try {
                        log.info("Order prepared to send inkatracker lite:{}",new ObjectMapper().writeValueAsString(b));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerLiteCreateOrderUri());

                    return WebClient
                            .builder()
                            .clientConnector(
                                    generateClientConnector(
                                            Integer.parseInt(externalServicesProperties.getInkatrackerLiteCreateOrderConnectTimeOut()),
                                            Integer.parseInt(externalServicesProperties.getInkatrackerLiteCreateOrderReadTimeOut())
                                    )
                            )
                            .baseUrl(externalServicesProperties.getInkatrackerLiteCreateOrderUri())
                            .build()
                            .post()
                            .body(Mono.just(b), OrderInkatrackerCanonical.class)
                            .exchange()
                            .flatMap(clientResponse -> mapResponseFromTracker(
                                    clientResponse, iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), externalId)
                            )
                            .doOnSuccess(s -> log.info("Response is Success in inkatracker-lite:{}",s))
                            .defaultIfEmpty(
                                    new OrderCanonical(
                                            iOrderFulfillment.getOrderId(),
                                            iOrderFulfillment.getEcommerceId(),
                                            objectToMapper.getOrderStatusInkatracker(
                                                    Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.name(), "Result inkatracker-lite is empty")
                                    )
                            )
                            .doOnError(e -> {
                                e.printStackTrace();
                                log.error("Error in inkatracker-lite:{}",e.getMessage());
                            })
                            .onErrorResume(e -> mapResponseErrorFromTracker(e, iOrderFulfillment.getOrderId(),
                                    iOrderFulfillment.getEcommerceId(), iOrderFulfillment.getStatusCode())
                            );

                }).doOnSuccess((f) ->  log.info("[END] Create Order To Tracker lite- orderCanonical:{}",f));

    }


    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        log.info("[START] connect inkatracker-lite   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        Constant.OrderStatusTracker orderStatusInkatracker = Constant.OrderStatusTracker.getByActionName(actionDto.getAction());

        log.info("url inkatracket-lite:{}",externalServicesProperties.getInkatrackerLiteUpdateOrderUri());

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getInkatrackerLiteUpdateOrderConnectTimeOut()),
                                Integer.parseInt(externalServicesProperties.getInkatrackerLiteUpdateOrderReadTimeOut())
                        )
                )
                .baseUrl(externalServicesProperties.getInkatrackerLiteUpdateOrderUri())
                .build()
                .patch()
                .uri(builder ->
                        builder
                                .path("/{orderExternalId}")
                                .queryParam("action",orderStatusInkatracker.getTrackerLiteStatus())
                                .queryParam("idCancellationReason",actionDto.getOrderCancelCode())
                                .build(ecommerceId))
                .retrieve()
                .bodyToMono(OrderInfoCanonical.class)
                .map(r -> {
                    log.info("response:{}", r);
                    OrderCanonical orderCanonical = new OrderCanonical();

                    Constant.OrderStatus orderStatusResult = orderStatusInkatracker.getOrderStatus();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(orderStatusResult.getCode());
                    orderStatus.setName(orderStatusResult.name());
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
                    orderCanonical.setOrderStatus(orderStatus);

                    return orderCanonical;
                })
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.getCode(),
                                Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.name())
                )
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error in inkatrackerlite call {} ",e.getMessage());
                    OrderCanonical orderCanonical = new OrderCanonical();
                    Constant.OrderStatus orderStatusResult = orderStatusInkatracker.getOrderStatusError();
                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(orderStatusResult.getCode());
                    orderStatus.setName(orderStatusResult.name());
                    orderStatus.setDetail(e.getMessage());
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                });
    }
}
