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
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
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

    private ApplicationParameterService applicationParameterService;
    private ExternalServicesProperties externalServicesProperties;
    private ObjectToMapper objectToMapper;

    public InkatrackerLiteServiceImpl(ExternalServicesProperties externalServicesProperties, ObjectToMapper objectToMapper,
                                      ApplicationParameterService applicationParameterService) {
        this.externalServicesProperties = externalServicesProperties;
        this.objectToMapper = objectToMapper;
        this.applicationParameterService = applicationParameterService;
    }


    @Override
    public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                                   List<IOrderItemFulfillment> itemFulfillments,
                                                   StoreCenterCanonical storeCenterCanonical,
                                                   Long externalId, String statusDetail, String statusName,
                                                   String orderCancelCode, String orderCancelObservation) {

        String dayToPickup = getApplicationParameter(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET);

        return Mono
                .just(objectToMapper.convertOrderToOrderInkatrackerCanonical(iOrderFulfillment, itemFulfillments,
                        storeCenterCanonical, externalId, statusName, statusDetail, orderCancelCode, orderCancelObservation)
                )
                .flatMap(b -> {

                    try {
                        b.setDaysToPickUp(dayToPickup);

                        log.info("Order prepared to send inkatracker lite:{}",
                                new ObjectMapper().writeValueAsString(b));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerLiteCreateOrderUri());

                    return WebClient
                            .builder()
                            .clientConnector(
                                    generateClientConnector(
                                            Integer.parseInt(externalServicesProperties.getInkatrackerLiteCreateOrderConnectTimeOut()),
                                            Long.parseLong(externalServicesProperties.getInkatrackerLiteCreateOrderReadTimeOut())
                                    )
                            )
                            .baseUrl(externalServicesProperties.getInkatrackerLiteCreateOrderUri())
                            .build()
                            .post()
                            .body(Mono.just(b), OrderInkatrackerCanonical.class)
                            .exchange()
                            .flatMap(clientResponse -> mapResponseFromTracker(
                                    clientResponse, iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), externalId, statusName)
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
                                Long.parseLong(externalServicesProperties.getInkatrackerLiteUpdateOrderReadTimeOut())
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
                .exchange()
                .flatMap(clientResponse -> mapResponseFromUpdateTracker(clientResponse, ecommerceId, orderStatusInkatracker))
                .doOnSuccess(s -> log.info("Response is Success in inkatracker-lite Update:{}",s))
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.getCode(),
                                Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.name())
                )
                .doOnError(e -> {
                    e.printStackTrace();
                    log.error("Error in inkatracker-lite when its sent to update:{}",e.getMessage());
                })
                .onErrorResume(e -> mapResponseErrorFromTracker(e, ecommerceId,
                        ecommerceId, orderStatusInkatracker.getOrderStatusError().name())
                ).flatMap(response -> {
                    // Bloque para enviar al order-tracker
                    /*

                        Si es (RET no lo envío al order-tracker o si el action es PICK_ORDER)
                            entonces no lo envío al order-tracker

                        SINO
                            Si el action es PREPARE y es DELIVERY
                            entonces lo envío al Order-tracker

                            Si el action es ASIGN_ORDER
                            entonces no lo envío al order-tracker

                            Si el action es ARRIVE_ORDER
                            entonces no lo envío al order-tracker

                            Si el action es ON_ROUTE_ORDER
                            entonces no lo envío al order-tracker

                            Si el action es DELIVER_ORDER, REJECTED o CANCELLED_ORDER y el origin es OMNI_DELIVERY
                            entonces no lo envío al order-tracker

                            Si el action es CANCEL_ORDER o DELIVER_ORDER y no tiene origin,
                            entonces lo envío al order-tracker

                            Si el action es CANCEL_ORDER y el origin es APP o WEB,
                            entonces lo envío al order-tracker

                     */

                });
    }
    private String getApplicationParameter(String code) {
        return applicationParameterService
                .getApplicationParameterByCodeIs(code).getValue();
    }
}
