package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.List;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service("temporary")
public class InkatrackerTemporaryServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;
    private ObjectToMapper objectToMapper;

    public InkatrackerTemporaryServiceImpl(ExternalServicesProperties externalServicesProperties,
                                           ObjectToMapper objectToMapper) {

        this.externalServicesProperties = externalServicesProperties;
        this.objectToMapper = objectToMapper;
    }


    @Override
    public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                                   List<IOrderItemFulfillment> itemFulfillments,
                                                   StoreCenterCanonical storeCenterCanonical,
                                                   Long externalId, String status, String statusDetail) {
        return Mono
                .just(objectToMapper
                        .convertOrderToOrderInkatrackerCanonical(
                                iOrderFulfillment, itemFulfillments, storeCenterCanonical, externalId, status
                        )
                )
                .flatMap(b -> {

                    log.info("Order prepared to send inkatracker - orderInkatracker:{}",b);

                    log.info("url inkatracker:{}",externalServicesProperties.getTemporaryCreateOrderUri());

                    return WebClient
                            .builder()
                            .clientConnector(
                                    generateClientConnector(
                                            Integer.parseInt(externalServicesProperties.getTemporaryCreateOrderConnectTimeOut()),
                                            Integer.parseInt(externalServicesProperties.getTemporaryCreateOrderReadTimeOut())
                                    )
                            )
                            .baseUrl(externalServicesProperties.getInkatrackerCreateOrderUri())
                            .build()
                            .post()
                            .body(Mono.just(b), OrderInkatrackerCanonical.class)
                            .exchange()
                            .flatMap(clientResponse -> mapResponseFromTracker(
                                    clientResponse, iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(),
                                    externalId, status, statusDetail)
                            )
                            .doOnSuccess(s -> log.info("Response is Success in inkatracker:{}",s))
                            .defaultIfEmpty(
                                    new OrderCanonical(
                                            iOrderFulfillment.getOrderId(),
                                            iOrderFulfillment.getEcommerceId(),
                                            objectToMapper.getOrderStatusInkatracker(
                                                    Constant.OrderStatus.EMPTY_RESULT_TEMPORARY.name(), "Result inkatracker is empty")
                                    )
                            )
                            .doOnError(e -> {
                                e.printStackTrace();
                                log.error("Error in inkatracker:{}",e.getMessage());
                            })
                            .onErrorResume(e -> mapResponseErrorFromTracker(e, iOrderFulfillment.getOrderId(),
                                    iOrderFulfillment.getEcommerceId(), iOrderFulfillment.getStatusCode())
                            );

                });


    }

}
