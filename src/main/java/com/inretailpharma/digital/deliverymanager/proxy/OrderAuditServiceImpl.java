package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
@Service("audit")
public class OrderAuditServiceImpl implements OrderExternalService {


    private final ExternalServicesProperties externalServicesProperties;

    public OrderAuditServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }


    @Override
    public void sendOrder(OrderFulfillmentCanonical orderAuditCanonical) {
        log.info("[START] connect api audit..  - value:{} - body:{}",
                externalServicesProperties, orderAuditCanonical);

        Flux<String> response = WebClient.create(externalServicesProperties.getUriApiService())
                .post().bodyValue(orderAuditCanonical).retrieve().bodyToFlux(String.class);
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
