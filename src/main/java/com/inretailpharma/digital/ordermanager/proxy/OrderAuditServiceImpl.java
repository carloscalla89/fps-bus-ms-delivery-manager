package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.audit.OrderAuditCanonical;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Slf4j
public class OrderAuditServiceImpl implements OrderAuditService {

    @Value("${external-service.audit.create-order}")
    private String externalServiceAuditCreateOrder;

    @Value("${external-service.audit.time-out}")
    private Integer timeout;

    private RestTemplate restTemplate;

    public OrderAuditServiceImpl() {
        restTemplate = new RestTemplate(getClientHttpRequestFactory());

    }

    @Override
    public void sendOrder(OrderAuditCanonical orderAuditCanonical) {

        Flux<String> response = WebClient.create().post().bodyValue(orderAuditCanonical).retrieve().bodyToFlux(String.class);

        response.subscribe(log::info);
        log.info("Exiting NON-BLOCKING Service!");
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
                = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setConnectTimeout(timeout);

        return clientHttpRequestFactory;
    }

}
