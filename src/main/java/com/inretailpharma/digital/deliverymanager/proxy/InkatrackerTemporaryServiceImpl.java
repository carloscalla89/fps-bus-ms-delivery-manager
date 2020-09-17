package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;


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
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {


        return null;

    }

    @Override
    public Mono<OrderCanonical> sendOrderToTracker(OrderCanonical order) {
        log.info("[START] Create Order To Tracker - orderCanonical:{}",order);

        return Mono
                .just(objectToMapper.convertOrderToOrderInkatrackerCanonical(order))
                .flatMap(b -> {

                log.info("Order prepared to send inkatracker:{}",b);

                log.info("url inkatracker:{}",externalServicesProperties.getTemporaryCreateOrderUri());

                TcpClient tcpClient = TcpClient
                        .create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                Integer.parseInt(externalServicesProperties.getTemporaryCreateOrderConnectTimeOut())
                        ) // Connection Timeout
                        .doOnConnected(connection ->
                                connection.addHandlerLast(
                                        new ReadTimeoutHandler(
                                                Integer.parseInt(externalServicesProperties.getTemporaryCreateOrderReadTimeOut())
                                        )
                                )
                        ); // Read Timeout

                return WebClient
                        .builder()
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                        .baseUrl(externalServicesProperties.getTemporaryCreateOrderUri())
                        .build()
                        .post()
                        .body(Mono.just(b), OrderInkatrackerCanonical.class)
                        .exchange()
                        .flatMap(clientResponse -> {
                            log.info("response temporary:{}, phrase:{}", clientResponse.statusCode(),
                                    clientResponse.statusCode().getReasonPhrase());

                            OrderCanonical orderCanonical = new OrderCanonical();
                            orderCanonical.setId(order.getId());
                            orderCanonical.setEcommerceId(order.getEcommerceId());
                            orderCanonical.setExternalId(order.getExternalId());

                            OrderStatusCanonical orderStatus;

                            if (clientResponse.statusCode().is2xxSuccessful()) {
                                orderCanonical.setTrackerId(order.getEcommerceId());
                                orderStatus = objectToMapper.getOrderStatusErrorCancel(Constant.OrderStatus.CONFIRMED_TRACKER.getCode(),
                                        clientResponse.statusCode().getReasonPhrase());

                                orderCanonical.setOrderStatus(orderStatus);

                                log.info("orderCanonical RESPONSE from temporary:{}",orderCanonical);
                                return Mono.just(orderCanonical);

                            } else {

                                return clientResponse
                                        .bodyToMono(String.class)
                                        .flatMap(msg -> Mono.error(new CustomException(msg,clientResponse.statusCode().value())));
                            }


                        })
                        .defaultIfEmpty(
                                new OrderCanonical(
                                        order.getId(),
                                        order.getEcommerceId(),
                                        objectToMapper.getOrderStatusErrorCancel(
                                                Constant.OrderStatus.EMPTY_RESULT_TEMPORARY.getCode(), "Result temporary is empty")
                                )
                        )
                        .onErrorResume(e -> {
                            e.printStackTrace();
                            log.error("Error in TEMPOARRY call {} ",e.getMessage());
                            OrderCanonical orderCanonical = new OrderCanonical();
                            orderCanonical.setEcommerceId(order.getEcommerceId());
                            orderCanonical.setId(order.getId());
                            OrderStatusCanonical orderStatus = objectToMapper
                                    .getOrderStatusErrorCancel(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode(), e.getMessage());

                            orderCanonical.setOrderStatus(orderStatus);

                            return Mono.just(orderCanonical);
                        });

            });
    }



}
