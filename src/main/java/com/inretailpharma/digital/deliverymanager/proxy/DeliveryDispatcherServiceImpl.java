package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.TrackerInsinkResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.TrackerResponseDto;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service("deliveryDispatcher")
public class DeliveryDispatcherServiceImpl implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;
    private RestTemplate restTemplate;

    public DeliveryDispatcherServiceImpl(ExternalServicesProperties externalServicesProperties,
                                         @Qualifier("dispatcherRestTemplate") RestTemplate restTemplate) {
        this.externalServicesProperties = externalServicesProperties;
        this.restTemplate = restTemplate;
    }

    @Override
    public Mono<OrderCanonical> sendOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public OrderCanonical sendOrder(OrderCanonical orderAuditCanonical) {
        return null;
    }

    @Override
    public OrderCanonical updateOrder(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> updateOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto) {
        log.info("update order actionOrder.getCode:{}", actionDto.getAction());

        OrderCanonical orderCanonical;

        switch (Constant.ActionOrder.getByName(actionDto.getAction()).getCode()) {
            case 1:
                // reattempt to send from delivery dispatcher at inkatracker or inkatrackerlite
                TrackerResponseDto trackerResponseDto = null;

                try {
                    log.info("Starting Connect Dispatcher uri action id 1: {}", externalServicesProperties.getDispatcherTrackerUri());

                    Map<String, String> uriParam = new HashMap<>();
                    uriParam.put("ecommerceId", ecommerceId.toString());

                    UriComponents builder = UriComponentsBuilder.fromHttpUrl(externalServicesProperties.getDispatcherTrackerUri())
                            .queryParam("action",actionDto.getAction())
                            .build();

                    trackerResponseDto =restTemplate.exchange(
                            builder.toString(),
                            HttpMethod.GET,
                            null,
                            TrackerResponseDto.class,
                            uriParam
                    ).getBody();

                    log.info("End Connect Dispatcher uri action id 1 with response - {}",trackerResponseDto);

                    orderCanonical = Optional
                                                .ofNullable(trackerResponseDto)
                                                .map(r -> {
                                                    OrderCanonical resultCanonical = new OrderCanonical();

                                                    resultCanonical.setTrackerId(r.getId());

                                                    Constant.OrderStatus orderStatusUtil = Optional.ofNullable(r.getId())
                                                            .map(s ->
                                                                    Optional
                                                                            .ofNullable(r.getCode())
                                                                            .map(code -> Constant.OrderStatus.getByName(code))
                                                                            .orElse(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS)
                                                            )
                                                            .orElseGet(() ->
                                                                    Constant.OrderStatus.getByName(
                                                                            Optional.ofNullable(r.getCode())
                                                                                    .orElse(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())
                                                                    )
                                                            );

                                                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                                    orderStatus.setCode(orderStatusUtil.getCode());
                                                    orderStatus.setName(orderStatusUtil.name());
                                                    orderStatus.setDetail(r.getDetail());

                                                    resultCanonical.setOrderStatus(orderStatus);

                                                    return resultCanonical;
                                                }).orElseGet(() -> {
                                                    OrderCanonical resultCanonical = new OrderCanonical();
                                                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                                    orderStatus.setCode(Constant.OrderStatus.NOT_DEFINED_ERROR.getCode());
                                                    orderStatus.setName(Constant.OrderStatus.NOT_DEFINED_ERROR.name());
                                                    resultCanonical.setOrderStatus(orderStatus);
                                                    return resultCanonical;
                                                });
                } catch (RestClientException e) {
                    e.printStackTrace();
                    String errorMessage = "Connection Error with DD: " +
                            "Error invoking '" +
                            externalServicesProperties.getDispatcherTrackerUri() + "':" + e.getMessage();
                    log.error(errorMessage);
                    orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                    orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                    orderStatus.setDetail(errorMessage);

                    orderCanonical.setOrderStatus(orderStatus);

                    //TODO check boolean used
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "General Error invoking '" + externalServicesProperties.getDispatcherTrackerUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);
                    orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                    orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                    orderStatus.setDetail(errorMessage);

                    orderCanonical.setOrderStatus(orderStatus);
                }

                break;

            case 2:
                // reattempt to send from delivery dispatcher at insink
                TrackerInsinkResponseCanonical trackerInsinkResponseCanonical;

                try {
                    log.info("Starting Connect Dispatcher uri action id 2: {}", externalServicesProperties.getDispatcherInsinkTrackerUri());

                    Map<String, String> uriParam = new HashMap<>();
                    uriParam.put("ecommerceId", ecommerceId.toString());

                    UriComponents builder = UriComponentsBuilder.fromHttpUrl(externalServicesProperties.getDispatcherInsinkTrackerUri())
                            .queryParam("action",actionDto.getAction())
                            .build();

                    trackerInsinkResponseCanonical =restTemplate.exchange(
                            builder.toString(),
                            HttpMethod.GET,
                            null,
                            TrackerInsinkResponseCanonical.class,
                            uriParam
                    ).getBody();

                    log.info("End Connect Dispatcher uri action id 2 with response - {}",trackerInsinkResponseCanonical);

                    orderCanonical = Optional
                                                .ofNullable(trackerInsinkResponseCanonical)
                                                .filter(r -> (r.getInsinkProcess() != null && r.getTrackerProcess() != null))
                                                .map(r -> {
                                                    OrderCanonical resultCanonical = new OrderCanonical();

                                                    if (r.getTrackerProcess() && r.getInsinkProcess()) {

                                                        resultCanonical.setTrackerId(r.getTrackerResponseDto().getId());
                                                        resultCanonical.setExternalId(
                                                                Optional
                                                                        .ofNullable(r.getInsinkResponseCanonical().getInkaventaId())
                                                                        .map(Long::parseLong).orElse(null)
                                                        );

                                                        Constant.OrderStatus orderStatusUtil = Optional.ofNullable(r.getInsinkResponseCanonical().getSuccessCode())
                                                                .filter(t -> t.equalsIgnoreCase("0-1") && resultCanonical.getExternalId() == null)
                                                                .map(t -> Constant.OrderStatus.SUCCESS_RESERVED_ORDER)
                                                                .orElse(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS);

                                                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                                        orderStatus.setCode(orderStatusUtil.getCode());
                                                        orderStatus.setName(orderStatusUtil.name());

                                                        resultCanonical.setOrderStatus(orderStatus);

                                                    } else if (r.getInsinkProcess() && !r.getTrackerProcess()) {
                                                        resultCanonical.setExternalId(
                                                                Optional
                                                                        .ofNullable(r.getInsinkResponseCanonical().getInkaventaId())
                                                                        .map(Long::parseLong).orElse(null)
                                                        );

                                                        Constant.OrderStatus orderStatusUtil = r.isReleased() ?
                                                                Constant.OrderStatus.ERROR_UPDATE_TRACKER_BILLING : Constant.OrderStatus.ERROR_INSERT_TRACKER;

                                                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                                        orderStatus.setCode(orderStatusUtil.getCode());
                                                        orderStatus.setName(orderStatusUtil.name());
                                                        orderStatus.setDetail(r.getTrackerResponseDto().getDetail());

                                                        resultCanonical.setOrderStatus(orderStatus);

                                                    } else {

                                                        Constant.OrderStatus orderStatusUtil = r.isReleased() ?
                                                                Constant.OrderStatus.ERROR_RELEASE_ORDER : Constant.OrderStatus.ERROR_INSERT_INKAVENTA;

                                                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                                        orderStatus.setCode(orderStatusUtil.getCode());
                                                        orderStatus.setName(orderStatusUtil.name());
                                                        orderStatus.setDetail(r.getInsinkResponseCanonical().getMessageDetail());

                                                        resultCanonical.setOrderStatus(orderStatus);
                                                    }

                                                    return resultCanonical;
                                                }).orElseGet(() -> {
                                                    OrderCanonical resultCanonical = new OrderCanonical();

                                                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                                    orderStatus.setCode(Constant.OrderStatus.NOT_DEFINED_ERROR.getCode());
                                                    orderStatus.setName(Constant.OrderStatus.NOT_DEFINED_ERROR.name());
                                                    resultCanonical.setOrderStatus(orderStatus);

                                                    return resultCanonical;
                                                });

                } catch (RestClientException e) {
                    String errorMessage = "Connection Error with DD: " +
                            "Error invoking '" +
                            externalServicesProperties.getDispatcherInsinkTrackerUri() + "':" + e.getMessage();
                    log.error(errorMessage);
                    orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                    orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                    orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                    orderStatus.setDetail(errorMessage);

                    orderCanonical.setOrderStatus(orderStatus);

                    //TODO check boolean used
                } catch (Exception e) {
                    e.printStackTrace();
                    String errorMessage = "General Error invoking '" + externalServicesProperties.getDispatcherInsinkTrackerUri() +
                            "':" + e.getMessage();
                    log.error(errorMessage);

                    orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                    orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                    orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                    orderStatus.setDetail(errorMessage);

                    orderCanonical.setOrderStatus(orderStatus);
                }

                break;

            default:
                orderCanonical = new OrderCanonical();

                OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());

                orderCanonical.setOrderStatus(orderStatus);

                break;
        }

        orderCanonical.setEcommerceId(ecommerceId);
        log.info("object parse order result {}", orderCanonical);

        return orderCanonical;
    }

}
