package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.InvoicedOrderCanonical;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderTrackerCanonical;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;

import java.util.ArrayList;
import java.util.List;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Service("inkatracker")
public class InkatrackerServiceImpl extends AbstractOrderService implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;
    private ObjectToMapper objectToMapper;



    public InkatrackerServiceImpl(ExternalServicesProperties externalServicesProperties,
                                  ObjectToMapper objectToMapper) {

        this.externalServicesProperties = externalServicesProperties;
        this.objectToMapper = objectToMapper;
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company,
                                                              String serviceType) {
        log.info("[START]  connect inkatracker   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        log.info("response action:{}", action);

        OrderTrackerCanonical orderTrackerCanonical = new OrderTrackerCanonical();
        List<InvoicedOrderCanonical> invoicedList = new ArrayList<>();
        if(actionDto.getInvoicedOrderList() != null) {
            actionDto.getInvoicedOrderList().forEach(i -> {
                InvoicedOrderCanonical invoiced = new InvoicedOrderCanonical();
                invoiced.setInvoicedNumber(i.getInvoicedNumber());
                invoicedList.add(invoiced);
            });
        }
        orderTrackerCanonical.setInvoicedList(invoicedList);
        orderTrackerCanonical.setInkaDeliveryId(actionDto.getExternalBillingId());
        orderTrackerCanonical.setCancelCode(actionDto.getOrderCancelCode());
        orderTrackerCanonical.setCancelClientReason(actionDto.getOrderCancelClientReason());
        orderTrackerCanonical.setCancelReason(actionDto.getOrderCancelReason());
        orderTrackerCanonical.setCancelObservation(actionDto.getOrderCancelObservation());

        Constant.OrderStatusTracker orderStatusInkatracker = Constant.OrderStatusTracker.getByActionName(actionDto.getAction());

        log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerUpdateStatusOrderUri());

        log.info("body:{}",orderTrackerCanonical);

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getInkatrackerUpdateStatusOrderConnectTimeOut()),
                                Long.parseLong(externalServicesProperties.getInkatrackerUpdateOrderReadTimeOut())
                        )
                )
                .baseUrl(externalServicesProperties.getInkatrackerUpdateStatusOrderUri())
                .build()
                .patch()
                .uri(builder -> builder
                                    .path("/{orderExternalId}")
                                    .queryParam("action",orderStatusInkatracker.getTrackerStatus())
                                    .build(ecommerceId))
                                    .body(Mono.just(orderTrackerCanonical), OrderTrackerCanonical.class)
                                    .exchange()
                                    .flatMap(clientResponse -> mapResponseFromUpdateTracker(clientResponse, ecommerceId, orderStatusInkatracker))
                                    .doOnSuccess(s -> log.info("Response is Success in inkatracker Update:{}",s))
                                    .defaultIfEmpty(
                                            new OrderCanonical(
                                                    ecommerceId,
                                                    Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.getCode(),
                                                    Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.name())
                                    )
                                    .doOnError(e -> {
                                        e.printStackTrace();
                                        log.error("Error in inkatracker when its sent to update:{}",e.getMessage());
                                    })
                                    .onErrorResume(e -> mapResponseErrorFromTracker(e, ecommerceId,
                                            ecommerceId, orderStatusInkatracker.getOrderStatusError().name(),
                                            actionDto.getOrderCancelCode(), actionDto.getOrderCancelObservation())
                                    );

    }

    @Override
    public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                                   List<IOrderItemFulfillment> itemFulfillments,
                                                   StoreCenterCanonical storeCenterCanonical,
                                                   Long externalId, String statusDetail, String statusName,
                                                   String orderCancelCode, String orderCancelObservation) {
        return Mono
                .just(objectToMapper.convertOrderToOrderInkatrackerCanonical(
                        iOrderFulfillment, itemFulfillments, storeCenterCanonical, externalId, statusName, statusDetail,
                        orderCancelCode, orderCancelObservation
                ))
                .flatMap(b -> {

                    log.info("Order prepared to send inkatracker - orderInkatracker:{}",b);

                    log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerCreateOrderUri());

                    return WebClient
                            .builder()
                            .clientConnector(
                                    generateClientConnector(
                                            Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderConnectTimeOut()),
                                            Long.parseLong(externalServicesProperties.getInkatrackerCreateOrderReadTimeOut())
                                    )
                            )
                            .baseUrl(externalServicesProperties.getInkatrackerCreateOrderUri())
                            .build()
                            .post()
                            .body(Mono.just(b), OrderInkatrackerCanonical.class)
                            .exchange()
                            .flatMap(clientResponse -> mapResponseFromTracker(
                                    clientResponse, iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(),
                                    externalId, statusName, orderCancelCode, orderCancelObservation)
                            )
                            .doOnSuccess(s -> log.info("Response is Success in inkatracker:{}",s))
                            .defaultIfEmpty(
                                    new OrderCanonical(
                                            iOrderFulfillment.getOrderId(),
                                            iOrderFulfillment.getEcommerceId(),
                                            objectToMapper.getOrderStatusInkatracker(
                                                    Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.name(),
                                                    "Result inkatracker is empty",
                                                    orderCancelCode,
                                                    orderCancelObservation
                                            )
                                    )
                            )
                            .doOnError(e -> {
                                e.printStackTrace();
                                log.error("Error in inkatracker:{}",e.getMessage());
                            })
                            .onErrorResume(e -> mapResponseErrorFromTracker(e, iOrderFulfillment.getOrderId(),
                                    iOrderFulfillment.getEcommerceId(), statusName, orderCancelCode, orderCancelObservation)
                            );

                });


    }
}
