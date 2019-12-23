package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.config.parameters.ExternalServicesProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
public class OrderAuditServiceImpl implements OrderAuditService {


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

    private ClientHttpRequestFactory getClientHttpRequestFactory() {

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(1000);

        return clientHttpRequestFactory;
    }

}
