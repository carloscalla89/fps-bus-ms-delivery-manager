package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.InvoicedOrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderTrackerCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service("temporary")
public class InkatrackerTemporaryServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;
    private ObjectToMapper objectToMapper;

    public InkatrackerTemporaryServiceImpl(ExternalServicesProperties externalServicesProperties,
                                           ObjectToMapper objectToMapper) {

        this.externalServicesProperties = externalServicesProperties;
        this.objectToMapper = objectToMapper;
    }


    @Override
    public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                                   List<IOrderItemFulfillment> itemFulfillments,
                                                   StoreCenterCanonical storeCenterCanonical,
                                                   Long externalId, String statusDetail, String statusName, String code, String obs) {
        return Mono
                .just(objectToMapper
                        .convertOrderToOrderInkatrackerCanonical(
                                iOrderFulfillment, itemFulfillments, storeCenterCanonical, externalId, Constant.OrderStatus.CONFIRMED_TRACKER.name(), null, null
                        )
                )
                .flatMap(b -> {

                    log.info("Order prepared to send inkatracker - orderInkatracker:{}",b);

                    log.info("url inkatracker:{}",externalServicesProperties.getTemporaryCreateOrderUri());

                    return WebClient
                            .builder()
                            .clientConnector(
                                    generateClientConnector(
                                            Integer.parseInt(externalServicesProperties.getTemporaryCreateOrderConnectTimeOut()),
                                            Integer.parseInt(externalServicesProperties.getTemporaryCreateOrderReadTimeOut())
                                    )
                            )
                            .baseUrl(externalServicesProperties.getInkatrackerCreateOrderUri())
                            .build()
                            .post()
                            .body(Mono.just(b), OrderInkatrackerCanonical.class)
                            .exchange()
                            .flatMap(clientResponse -> mapResponseFromTracker(
                                    clientResponse, iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), externalId)
                            )
                            .doOnSuccess(s -> log.info("Response is Success in inkatracker:{}",s))
                            .defaultIfEmpty(
                                    new OrderCanonical(
                                            iOrderFulfillment.getOrderId(),
                                            iOrderFulfillment.getEcommerceId(),
                                            objectToMapper.getOrderStatusInkatracker(
                                                    Constant.OrderStatus.EMPTY_RESULT_TEMPORARY.name(), "Result inkatracker is empty")
                                    )
                            )
                            .doOnError(e -> {
                                e.printStackTrace();
                                log.error("Error in inkatracker:{}",e.getMessage());
                            })
                            .onErrorResume(e -> mapResponseErrorFromTracker(e, iOrderFulfillment.getOrderId(),
                                    iOrderFulfillment.getEcommerceId(), iOrderFulfillment.getStatusCode())
                            );

                });
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        log.info("[START]  connect inkatracker   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        log.info("response action:{}", action);

        OrderTrackerCanonical orderTrackerCanonical = new OrderTrackerCanonical();
        orderTrackerCanonical.setInkaDeliveryId(actionDto.getExternalBillingId());
        orderTrackerCanonical.setCancelCode(actionDto.getOrderCancelCode());
        orderTrackerCanonical.setCancelClientReason(actionDto.getOrderCancelClientReason());
        orderTrackerCanonical.setCancelReason(actionDto.getOrderCancelReason());
        orderTrackerCanonical.setCancelObservation(actionDto.getOrderCancelObservation());


        log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerUpdateStatusOrderUri());

        log.info("body:{}",orderTrackerCanonical);

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getInkatrackerUpdateStatusOrderConnectTimeOut()),
                                Integer.parseInt(externalServicesProperties.getInkatrackerUpdateOrderReadTimeOut())
                        )
                )
                .baseUrl(externalServicesProperties.getInkatrackerUpdateStatusOrderUri())
                .build()
                .patch()
                .uri(builder -> builder
                        .path("/{orderExternalId}")
                        .queryParam("action",actionDto.getAction())
                        .build(ecommerceId))
                .body(Mono.just(orderTrackerCanonical), OrderTrackerCanonical.class)
                .exchange()
                .map(r -> {
                    log.info("response r :{}", r);
                    log.info("response action :{}", action);
                    log.info("response:{}", r.statusCode());

                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setExternalId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));

                    Constant.OrderStatus os;
                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();


                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                    orderCanonical.setOrderStatus(orderStatus);

                    log.info("orderCanonical:{}",orderCanonical);

                    return orderCanonical;
                })
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.EMPTY_RESULT_TEMPORARY.getCode(),
                                Constant.OrderStatus.EMPTY_RESULT_TEMPORARY.name())
                )
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error in inkatracker call {} ",e.getMessage());

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                    orderStatus.setDetail(e.getMessage());
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                });

    }

}
