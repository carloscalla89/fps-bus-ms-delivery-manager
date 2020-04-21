package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.UnassignedCanonical;
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
    public Mono<Void> sendOrderReactive(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to createOrder - uri:{}",
                externalServicesProperties.getUriApiService());

        return Mono
                .justOrEmpty(
                        applicationParameterService
                                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_AUDIT)
                )
                .filter(r -> {
                    log.info("Parameter to call uS-Audit:{}",r.getValue());

                    return r.getValue().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE);
                }).map(r -> WebClient
                                .create(externalServicesProperties.getUriApiService())
                                .post()
                                .body(Mono.just(orderAuditCanonical), OrderCanonical.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .retry(3)
                                .subscribeOn(Schedulers.parallel())
                                .subscribe(s -> log.info("[END] service to call api audit to createOrder - s:{}",s))
                ).then();

    }


    @Override
    public Mono<Void> updateOrderReactive(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to update - uri:{}",
                externalServicesProperties.getUriApiService());

        return Mono
                .justOrEmpty(
                        applicationParameterService
                                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_AUDIT)
                )
                .filter(r -> {
                    log.info("Parameter to call uS-Audit:{}",r.getValue());

                    return r.getValue().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE);
                })
                .map(r -> WebClient
                            .create(externalServicesProperties.getUriApiService())
                            .patch()
                            .body(Mono.just(orderAuditCanonical), OrderCanonical.class)
                            .retrieve()
                            .bodyToMono(String.class)
                            .retry(3)
                            .subscribeOn(Schedulers.parallel())
                            .subscribe(s -> log.info("[END] service to call api audit to update - s:{}",s))
                ).then();


    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        return null;
    }

    @Override
    public Mono<Void> sendOrderToTracker(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
	public Mono<Void> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {
		return null;
	}

	@Override
	public Mono<Void> unassignOrders(UnassignedCanonical unassignedCanonical) {
		return null;
	}
}
