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
    public Mono<OrderCanonical> sendOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> sendOrderReactiveWithOrderDto(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> updateOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto) {
        log.info("[START] connect inkatracker-lite   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        String actionInkatrackerLite;
        Constant.OrderStatus pending;
        Constant.OrderStatus successResponse;

        switch (actionDto.getAction()) {

            case Constant.ActionName.RELEASE_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_BILLING;
                pending = Constant.OrderStatus.PENDING_RELEASE_ORDER;
                successResponse = Constant.OrderStatus.RELEASED_ORDER;
                break;
            case Constant.ActionName.CANCEL_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.CANCELLED;
                pending = Constant.OrderStatus.PENDING_CANCEL_ORDER;
                successResponse = Constant.OrderStatus.CANCELLED_ORDER;
                break;
            case Constant.ActionName.DELIVER_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.DELIVERED;
                pending = Constant.OrderStatus.PENDING_DELIVERY_ORDER;
                successResponse = Constant.OrderStatus.DELIVERED_ORDER;
                break;
            case Constant.ActionName.READY_PICKUP_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_PICKUP;
                pending = Constant.OrderStatus.PENDING_READY_PICKUP_ORDER;
                successResponse = Constant.OrderStatus.READY_PICKUP_ORDER;
                break;
            default:
                actionInkatrackerLite = Constant.OrderStatus.NOT_FOUND_ACTION.name();
                pending = Constant.OrderStatus.NOT_DEFINED_STATUS;
                successResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;
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
                    orderStatus.setCode(Constant.OrderStatus.ERROR_UPDATE_ORDER.getCode());
                    orderStatus.setName(Constant.OrderStatus.ERROR_UPDATE_ORDER.name());
                    orderStatus.setDetail(e.getMessage());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                }).block();

        /*
        webClient.subscribe(r -> {
            log.info("Response inkatracker lite {} ",r);

            try {

                r.setEcommerceId(ecommerceId);

                ApplicationParameter activatedAudit = applicationParameterService
                        .findApplicationParameterByCode(Constant.ApplicationsParameters.ACTIVATED_AUDIT);

                log.info("Call for uS-Audit:{}",activatedAudit);

                Optional
                        .ofNullable(activatedAudit)
                        .map(s -> s.getCode().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE))
                        .ifPresent(s -> orderExternalServiceAudit.updateOrder(r));

            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error to send at uS-Audit to update error:{}",e.getMessage());
            }
        });
         */

    }
}
