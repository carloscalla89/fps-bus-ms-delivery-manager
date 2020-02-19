package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ResponseDTO;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

@Slf4j
@Service("orderTracker")
public class OrderTrackerServiceImpl implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    public OrderTrackerServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public Mono<OrderCanonical> sendOrderReactive(OrderCanonical orderCanonical) {
        return null;

    }

    @Override
    public Mono<OrderCanonical> sendOrderReactiveWithOrderDto(OrderCanonical orderCanonical) {

        log.info("[START] sendOrderReactive Order-Tracker ");

        return WebClient
                .create(externalServicesProperties.getOrderTrackerCreateOrderUri())
                .post()
                .body(Mono.just(orderCanonical), OrderCanonical.class)
                .retrieve()
                .bodyToMono(ResponseDTO.class)
                .subscribeOn(Schedulers.parallel())
                .map(r -> {
                    log.info("Response Order-Tracker r:{}",r);
                    Constant.OrderStatus status = Optional
                            .ofNullable(r.getCode())
                            .filter(s -> s.equals(Constant.OrderTrackerResponseCode.SUCCESS_CODE))
                            .map(s-> Constant.OrderStatus.SHIPPED_TRACKER_ORDER)
                            .orElse(Constant.OrderStatus.ERROR_SHIPPER_TRACKER_ORDER);

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(status.getCode());
                    orderStatus.setName(status.name());
                    orderStatus.setDetail(r.getErrorDetail());

                    orderCanonical.setOrderStatus(orderStatus);

                    return orderCanonical;


                })
                .onErrorResume(e -> {
                    log.error("[END] Error calling uS-Order-Tracker: {} ",e.getMessage());

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.ERROR_SHIPPER_TRACKER_ORDER.getCode());
                    orderStatus.setName(Constant.OrderStatus.ERROR_SHIPPER_TRACKER_ORDER.name());
                    orderStatus.setDetail(e.getMessage());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                }).doOnSuccess(s -> log.info("[END] sendOrderReactive Order-Tracker"));
    }


    @Override
    public Mono<OrderCanonical> updateOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto) {
        return null;
    }
}
