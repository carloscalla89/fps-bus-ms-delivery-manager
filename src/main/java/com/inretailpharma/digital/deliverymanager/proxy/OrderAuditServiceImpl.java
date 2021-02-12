package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Slf4j
@Service("audit")
public class OrderAuditServiceImpl extends AbstractOrderService  implements OrderExternalService {

    private final ExternalServicesProperties externalServicesProperties;

    public OrderAuditServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }


    @Override
    public Mono<Void> sendOrderReactive(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to create - uri:{}, ecommerceId:{}, statusCode:{}, statusName:{}," +
                        "statusDetail:{}", externalServicesProperties.getUriApiService(), orderAuditCanonical.getEcommerceId(),
                orderAuditCanonical.getOrderStatus().getCode(), orderAuditCanonical.getOrderStatus().getName(),
                orderAuditCanonical.getOrderStatus().getDetail());

        return WebClient
                .create(externalServicesProperties.getUriApiService())
                .post()
                .body(Mono.just(orderAuditCanonical), OrderCanonical.class)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3)
                .subscribeOn(Schedulers.parallel())
                .doOnSuccess((r) -> log.info("[END] service to call api audit to createOrder:{}",r))
                .then();


    }


    @Override
    public Mono<Void> updateOrderReactive(OrderCanonical orderAuditCanonical) {
        log.info("[START] service to call api audit to update - uri:{}, ecommerceId:{}, statusCode:{}, statusName:{}," +
                  "statusDetail:{}", externalServicesProperties.getUriApiService(), orderAuditCanonical.getEcommerceId(),
                orderAuditCanonical.getOrderStatus().getCode(), orderAuditCanonical.getOrderStatus().getName(),
                orderAuditCanonical.getOrderStatus().getDetail());

        return WebClient
                .create(externalServicesProperties.getUriApiService())
                .patch()
                .body(Mono.just(orderAuditCanonical), OrderCanonical.class)
                .retrieve()
                .bodyToMono(String.class)
                .retry(3)
                .subscribeOn(Schedulers.parallel())
                .doOnSuccess((r) -> log.info("[END] service to call api audit to update:{}",r))
                .then();

    }
    @Override
    public Mono<Void> updateOrderNewAudit(AuditHistoryDto auditHistoryDto) {
        log.info("[START] service to call api audit new history: uri{}, dto:{}",
                externalServicesProperties.getUriUpdateHistoryAuditApiService(), auditHistoryDto);

        return WebClient
                .create(externalServicesProperties.getUriUpdateHistoryAuditApiService())
                .patch()
                .body(Mono.just(auditHistoryDto), OrderCanonical.class)
                .exchange()
                .flatMap(clientResponse -> clientResponse.bodyToMono(Void.class))
                .retry(3)
                .subscribeOn(Schedulers.parallel())
                .switchIfEmpty(Mono.defer(() -> {
                    log.error("Call audit new history is empty - ecommerceId:{}",auditHistoryDto.getEcommerceId());
                    return Mono.when();
                }))
                .doOnError(e -> {
                    e.printStackTrace();
                    log.error("Error to call audit new history with ecommerceId:{} and error:{}",
                            auditHistoryDto.getEcommerceId(),e.getMessage());
                })
                .doOnSuccess((r) -> log.info("[END] service to call api audit history to update with ecommerceId:{},{}",
                        auditHistoryDto.getEcommerceId(),r))
                .then();
    }

}
