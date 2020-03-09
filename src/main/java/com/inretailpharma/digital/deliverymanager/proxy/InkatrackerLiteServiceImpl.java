package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service("inkatrackerlite")
public class InkatrackerLiteServiceImpl implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    public InkatrackerLiteServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public Mono<Void> sendOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> sendOrderReactiveWithOrderDto(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<Void> updateOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto) {
        log.info("[START] connect inkatracker-lite   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        String actionInkatrackerLite;
        Constant.OrderStatus errorResponse;
        Constant.OrderStatus successResponse;

        switch (actionDto.getAction()) {

            case Constant.ActionName.RELEASE_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_BILLING;
                successResponse = Constant.OrderStatus.RELEASED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_RELEASE_ORDER;
                break;
            case Constant.ActionName.CANCEL_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.CANCELLED;
                successResponse = Constant.OrderStatus.CANCELLED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_CANCEL;
                break;
            case Constant.ActionName.DELIVER_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.DELIVERED;
                successResponse = Constant.OrderStatus.DELIVERED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_DELIVER;
                break;
            case Constant.ActionName.READY_PICKUP_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_PICKUP;
                successResponse = Constant.OrderStatus.READY_PICKUP_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_PICKUP;
                break;
            default:
                actionInkatrackerLite = Constant.OrderStatus.NOT_FOUND_ACTION.name();
                successResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;
                errorResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;
        }


        return WebClient
                .create(externalServicesProperties.getInkatrackerLiteUpdateOrderUri())
                .patch()
                .uri(builder ->
                        builder
                                .path("/{orderExternalId}")
                                .queryParam("action",actionInkatrackerLite)
                                .queryParam("idCancellationReason",actionDto.getOrderCancelCode())
                                .build(ecommerceId))
                .retrieve()
                .bodyToMono(OrderInfoCanonical.class)
                .subscribeOn(Schedulers.parallel())
                .map(r -> {
                    OrderCanonical orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(successResponse.getCode());
                    orderStatus.setName(successResponse.name());
                    orderCanonical.setOrderStatus(orderStatus);

                    return orderCanonical;
                })
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error in inkatrackerlite call {} ",e.getMessage());
                    OrderCanonical orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(errorResponse.getCode());
                    orderStatus.setName(errorResponse.name());
                    orderStatus.setDetail(e.getMessage());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                }).block();

    }
}
