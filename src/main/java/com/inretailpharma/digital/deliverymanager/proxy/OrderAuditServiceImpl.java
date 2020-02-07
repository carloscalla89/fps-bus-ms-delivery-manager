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
    public void sendOrder(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to createOrder - value:{} - body:{}",
                externalServicesProperties, orderAuditCanonical);

        try {

            ApplicationParameter activatedAudit = applicationParameterService
                    .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE);

            log.info("Parameter to Call uS-Audit - activated=1 - Not activated=0 activatedAudit-{}",activatedAudit);

            Optional
                    .ofNullable(activatedAudit)
                    .filter(s -> s.getValue().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE))
                    .ifPresent(s -> {

                        Mono<String> response = WebClient
                                .create(externalServicesProperties.getUriApiService())
                                .post()
                                .body(Mono.just(orderAuditCanonical), OrderCanonical.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .map(r -> r)
                                .onErrorResume(e -> {
                                    e.printStackTrace();
                                    log.error("Error in audit call {} ",e.getMessage());

                                    return Mono.just("ERROR");
                                });

                        response.subscribe(log::info);
                        log.info("[END] Exiting NON-BLOCKING service to call api audit to createOrder");

                    });


        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error to send at uS-Audit - error:{}",e.getMessage());
        }


    }

    @Override
    public void updateOrder(OrderCanonical orderCanonical) {
        log.info("[START] service to call api audit to updateOrder  - value:{} - body:{}",
                externalServicesProperties, orderCanonical);

        try {

            ApplicationParameter activatedAudit = applicationParameterService
                    .findApplicationParameterByCode(Constant.ApplicationsParameters.ACTIVATED_AUDIT);

            log.info("Parameter to Call uS-Audit - activated=1 - Not activated=0 activatedAudit-{}", activatedAudit);

            Optional
                    .ofNullable(activatedAudit)
                    .filter(s -> s.getValue().equalsIgnoreCase(Constant.ApplicationsParameters.ACTIVATED_AUDIT_VALUE))
                    .ifPresent(s -> {

                        Mono<String> response = WebClient
                                .create(externalServicesProperties.getUriApiService())
                                .patch()
                                .body(Mono.just(orderCanonical), OrderCanonical.class)
                                .retrieve()
                                .bodyToMono(String.class)
                                .map(r -> r)
                                .onErrorResume(e -> {
                                    e.printStackTrace();
                                    log.error("Error in audit call {} ",e.getMessage());

                                    return Mono.just("ERROR");
                                });

                        response.subscribe(log::info);
                        log.info("[END] Exiting NON-BLOCKING service to call api audit to updateOrder ");

                    });

        } catch (Exception e) {
            e.printStackTrace();
            log.error("Error to send at uS-Audit - error:{}",e.getMessage());
        }

    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, ActionDto actionDto) {
        return null;
    }



}
