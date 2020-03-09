package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ResponseDTO;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
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
    private ApplicationParameterService applicationParameterService;

    public OrderTrackerServiceImpl(ExternalServicesProperties externalServicesProperties,
                                   ApplicationParameterService applicationParameterService) {
        this.externalServicesProperties = externalServicesProperties;
        this.applicationParameterService = applicationParameterService;
    }

    @Override
    public Mono<Void> sendOrderReactive(OrderCanonical orderCanonical) {
        return null;

    }

    @Override
    public Mono<OrderCanonical> sendOrderReactiveWithOrderDto(OrderCanonical orderCanonical) {

        log.info("[START] sendOrderReactive Order-Tracker ");

        ApplicationParameter activatedAudit = applicationParameterService
                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_ORDER_TRACKER);

        log.info("Parameter to Call uS-Audit - activated=1 - Not activated=0 activatedAudit-{}",activatedAudit);

        return Optional
                .ofNullable(applicationParameterService
                        .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_ORDER_TRACKER))
                .filter(r -> r.getValue().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_ORDER_TRACKER_VALUE))
                .map(o -> WebClient
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
                                    .map(s-> Constant.OrderStatus.ERROR_ON_STORE)
                                    .orElse(Constant.OrderStatus.ERROR_ON_STORE);

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
                            orderStatus.setCode(Constant.OrderStatus.ERROR_ON_STORE.getCode());
                            orderStatus.setName(Constant.OrderStatus.ERROR_ON_STORE.name());
                            orderStatus.setDetail(e.getMessage());

                            orderCanonical.setOrderStatus(orderStatus);

                            return Mono.just(orderCanonical);
                        }).doOnSuccess(s -> log.info("[END] sendOrderReactive Order-Tracker"))).orElseGet(() -> Mono.just(orderCanonical));
    }


    @Override
    public Mono<Void> updateOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto) {
        return null;
    }
}
