package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
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
@Service("audit")
public class OrderAuditServiceImpl implements OrderExternalService {

    private final ExternalServicesProperties externalServicesProperties;
    private ApplicationParameterService applicationParameterService;

    public OrderAuditServiceImpl(ExternalServicesProperties externalServicesProperties,
                                 ApplicationParameterService applicationParameterService) {
        this.externalServicesProperties = externalServicesProperties;
        this.applicationParameterService = applicationParameterService;
    }


    @Override
    public Mono<OrderCanonical> sendOrderReactive(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to createOrder - uri:{} - body:{}",
                externalServicesProperties.getUriApiService(), orderAuditCanonical);

        ApplicationParameter activatedAudit = applicationParameterService
                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_AUDIT);

        log.info("Parameter to Call uS-Audit - activated=1 - Not activated=0 activatedAudit-{}",activatedAudit);

        Optional
                .ofNullable(applicationParameterService
                        .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_AUDIT))
                .filter(r -> r.getValue().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE))
                .ifPresent(r -> WebClient
                        .create(externalServicesProperties.getUriApiService())
                        .post()
                        .body(Mono.just(orderAuditCanonical), OrderCanonical.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribeOn(Schedulers.parallel())
                        .subscribe(s -> log.info("[END] service to call api audit to createOrder - s:{}",s)));


        return Mono.just(orderAuditCanonical);
    }

    @Override
    public Mono<OrderCanonical> sendOrderReactiveWithOrderDto(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> updateOrderReactive(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to createOrder - value:{} - body:{}",
                externalServicesProperties, orderAuditCanonical);

        ApplicationParameter activatedAudit = applicationParameterService
                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_AUDIT);

        log.info("Parameter to Call uS-Audit - activated=1 - Not activated=0 activatedAudit-{}",activatedAudit);

        Optional
                .ofNullable(applicationParameterService
                        .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_AUDIT))
                .filter(r -> r.getValue().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE))
                .ifPresent(r -> WebClient
                        .create(externalServicesProperties.getUriApiService())
                        .patch()
                        .body(Mono.just(orderAuditCanonical), OrderCanonical.class)
                        .retrieve()
                        .bodyToMono(String.class)
                        .subscribeOn(Schedulers.parallel())
                        .subscribe(s -> log.info("[END] service to call api audit to createOrder - s:{}",s)));


        return Mono.just(orderAuditCanonical);
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto) {
        return null;
    }



}
