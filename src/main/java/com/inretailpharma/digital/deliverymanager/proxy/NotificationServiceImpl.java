package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.notification.MessageDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service("notification")
public class NotificationServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    @Autowired
    public NotificationServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public Mono<Void> sendNotification(MessageDto messageDto) {
        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getNotificationLambdaConnectTimeOut()),
                                Long.parseLong(externalServicesProperties.getNotificationLambdaReadTimeOut())
                        )
                )
                .baseUrl(externalServicesProperties.getNotificationLambdaUri())
                .build()
                .post()
                .body(Mono.just(messageDto), MessageDto.class)
                .exchange()
                .subscribeOn(Schedulers.parallel())
                .doOnSuccess(s -> log.info("Response notification is Success:{} with orderId:{}",s, messageDto.getOrderId()))
                .doOnError(e -> {
                    e.printStackTrace();
                    log.error("Error to send lambda notification:{} with orderId:{}",e.getMessage(), messageDto.getOrderId());
                })
                .then();
    }
}
