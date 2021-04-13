package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.TrackerInsinkResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.EcommerceMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service("deliveryDispatcherMifa")
public class DeliveryDispatcherMifaServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ApplicationParameterService applicationParameterService;
    private ExternalServicesProperties externalServicesProperties;
    private EcommerceMapper ecommerceMapper;

    public DeliveryDispatcherMifaServiceImpl(ApplicationParameterService applicationParameterService,
                                             ExternalServicesProperties externalServicesProperties,
                                             EcommerceMapper ecommerceMapper) {
        this.applicationParameterService = applicationParameterService;
        this.externalServicesProperties = externalServicesProperties;
        this.ecommerceMapper = ecommerceMapper;
    }


    @Override
    public Mono<OrderCanonical> sendOrderEcommerce(IOrderFulfillment iOrderFulfillment, List<IOrderItemFulfillment> itemFulfillments,
                                                   String action, StoreCenterCanonical storeCenterCanonical) {

        String dispatcherUri;

        ApplicationParameter applicationParameter = applicationParameterService
                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_DD_MF);


        if (Constant.Logical.getByValueString(applicationParameter.getValue()).value()) {
            dispatcherUri = externalServicesProperties.getDispatcherLegacySystemUriMifarma();

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

        } else {
            dispatcherUri = externalServicesProperties.getDispatcherInsinkTrackerUriMiFarma();

            log.info("url dispatcher new desactivated:{} - company:{}", dispatcherUri, iOrderFulfillment.getCompanyCode());

            return WebClient
                    .builder()
                    .clientConnector(
                            generateClientConnector(
                                    Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerConnectTimeout()),
                                    Long.parseLong(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout())
                            )
                    )
                    .baseUrl(dispatcherUri)
                    .build()
                    .get()
                    .uri(builder ->
                            builder
                                    .path("/{orderId}")
                                    .queryParam("action", action)
                                    .build(iOrderFulfillment.getEcommerceId()))
                    .retrieve()
                    .bodyToMono(TrackerInsinkResponseCanonical.class)
                    .subscribeOn(Schedulers.parallel())
                    .filter(r -> (r.getInsinkProcess() != null && r.getTrackerProcess() != null))
                    .map(r -> {
                        log.info("result dispatcher to reattempt insink and tracker r:{}", r);
                        OrderCanonical resultCanonical = new OrderCanonical();
                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                        if (r.getInsinkProcess()) {

                            resultCanonical.setExternalId(
                                    Optional
                                            .ofNullable(r.getInsinkResponseCanonical().getInkaventaId())
                                            .map(Long::parseLong).orElse(null)
                            );

                            Constant.OrderStatus orderStatusUtil = Optional.ofNullable(r.getInsinkResponseCanonical().getSuccessCode())
                                    .filter(t -> t.equalsIgnoreCase("0-1") && resultCanonical.getExternalId() == null)
                                    .map(t -> Constant.OrderStatus.SUCCESS_RESERVED_ORDER)
                                    .orElse(Constant.OrderStatus.CONFIRMED);

                            orderStatus.setCode(orderStatusUtil.getCode());
                            orderStatus.setName(orderStatusUtil.name());

                        } else {

                            Constant.OrderStatus orderStatusUtil = Constant.OrderStatus.ERROR_INSERT_INKAVENTA;

                            if (r.getInsinkResponseCanonical() != null && r.getInsinkResponseCanonical().getErrorCode() != null &&
                                    r.getInsinkResponseCanonical().getErrorCode().equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)) {
                                orderStatusUtil = Constant.OrderStatus.CANCELLED_ORDER;

                            }

                            orderStatus.setCode(orderStatusUtil.getCode());
                            orderStatus.setName(orderStatusUtil.name());

                            Optional.ofNullable(r.getInsinkResponseCanonical())
                                    .ifPresent(z -> orderStatus.setDetail(z.getMessageDetail()));

                        }

                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
                        resultCanonical.setOrderStatus(orderStatus);
                        resultCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());

                        return resultCanonical;
                    })
                    .defaultIfEmpty(
                            new OrderCanonical(
                                    iOrderFulfillment.getEcommerceId(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                    )
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        String errorMessage = "Error to invoking'" + dispatcherUri +
                                "':" + e.getMessage();
                        log.error(errorMessage);

                        OrderCanonical orderCanonical = new OrderCanonical();

                        orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                        orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                        orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                        orderStatus.setDetail(errorMessage);
                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                        orderCanonical.setOrderStatus(orderStatus);

                        return Mono.just(orderCanonical);
                    });

        }
    }

    public Mono<com.inretailpharma.digital.deliverymanager.dto.OrderDto> getOrderFromEcommerce(Long ecommerceId) {
        log.info("[START] getOrderFromEcommerce from mifa:{}",ecommerceId);

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getDispatcherOrderEcommerceConnectTimeout()),
                                Long.parseLong(externalServicesProperties.getDispatcherOrderEcommerceReadTimeout())
                        )
                )
                .baseUrl(externalServicesProperties.getDispatcherOrderEcommerceUriMifarma())
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
                    return Mono.error(new CustomException("Error to get order from ecommerce mifa",clientResponse.statusCode().value()));

                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    String errorMessage = "Error to invoking Delivery-dispatcher from mifa'"
                            + externalServicesProperties.getDispatcherOrderEcommerceUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);

                    return Mono.empty();
                });
    }

}
