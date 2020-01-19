package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service("audit")
public class OrderAuditServiceImpl implements OrderExternalService {


    private final ExternalServicesProperties externalServicesProperties;

    public OrderAuditServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }


    @Override
    public void sendOrder(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to createOrder - value:{} - body:{}",
                externalServicesProperties, orderAuditCanonical);

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
    }

    @Override
    public void updateOrder(OrderCanonical orderCanonical) {
        log.info("[START] service to call api audit to updateOrder  - value:{} - body:{}",
                externalServicesProperties, orderCanonical);

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
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, Constant.ActionOrder actionOrder) {
        return null;
    }



}
