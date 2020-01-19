package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
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
        log.info("[START] connect api audit..  - value:{} - body:{}",
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
        log.info("Exiting NON-BLOCKING Service!");
    }

    @Override
    public void updateOrder(OrderCanonical orderCanonical) {
        log.info("[START] connect api audit to update..  - value:{} - body:{}",
                externalServicesProperties, orderCanonical);

        Flux<String> response = WebClient.create(externalServicesProperties.getUriApiService())
                .patch().bodyValue(orderCanonical).retrieve().bodyToFlux(String.class);
        response.subscribe(log::info);
        log.info("Exiting NON-BLOCKING Service!");
    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, Constant.ActionOrder actionOrder) {
        return null;
    }



}
