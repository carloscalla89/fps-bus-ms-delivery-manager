package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderStatusInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
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

import java.util.Optional;

@Slf4j
@Service("inkatracker")
public class InkatrackerServiceImpl implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;

    public InkatrackerServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public Mono<Void> sendOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<Void> updateOrderReactive(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        log.info("[START] connect inkatracker   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        Constant.OrderStatus errorResponse;
        Constant.OrderStatus successResponse;

        OrderStatusInkatrackerCanonical orderInkaTrackerStatus = new OrderStatusInkatrackerCanonical();
        switch (actionDto.getAction()) {

            case Constant.ActionName.CANCEL_ORDER:
                orderInkaTrackerStatus.setStatusName(Constant.ActionNameInkatrackerlite.CANCELLED);
                successResponse = Constant.OrderStatus.CANCELLED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_TO_CANCEL_ORDER;
                break;
            case Constant.ActionName.DELIVER_ORDER:
                orderInkaTrackerStatus.setStatusName(Constant.ActionNameInkatrackerlite.DELIVERED);
                successResponse = Constant.OrderStatus.DELIVERED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_DELIVER;
                break;
            default:
                orderInkaTrackerStatus.setStatusName(Constant.OrderStatus.NOT_FOUND_ACTION.name());
                successResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;
                errorResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;
        }
        OrderInkatrackerCanonical orderInkatrackerCanonical = new OrderInkatrackerCanonical();
        orderInkatrackerCanonical.setOrderExternalId(ecommerceId);
        orderInkatrackerCanonical.setOrderStatus(orderInkaTrackerStatus);
        orderInkatrackerCanonical.setInkaDeliveryId(
                Optional.ofNullable(actionDto.getExternalBillingId())
                        .map(Long::parseLong).orElse(0L)
        );

        log.info("url inkatracket:{}",externalServicesProperties.getInkatrackerUpdateOrderUri());

        TcpClient tcpClient = TcpClient
                .create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        Integer.parseInt(externalServicesProperties.getInkatrackerLiteUpdateOrderConnectTimeOut())
                ) // Connection Timeout
                .doOnConnected(connection ->
                        connection.addHandlerLast(
                                new ReadTimeoutHandler(
                                        Integer.parseInt(externalServicesProperties.getInkatrackerLiteUpdateOrderReadTimeOut())
                                )
                        )
                ); // Read Timeout

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(externalServicesProperties.getInkatrackerLiteUpdateOrderUri())
                .build()
                .post()
                .body(Mono.just(orderInkatrackerCanonical), OrderInkatrackerCanonical.class)
                .exchange()
                .map(r -> {
                    log.info("response:{}", r);

                    OrderCanonical orderCanonical = new OrderCanonical();
                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                    if (r.statusCode().is2xxSuccessful()) {
                        orderStatus.setCode(successResponse.getCode());
                        orderStatus.setName(successResponse.name());
                        orderCanonical.setOrderStatus(orderStatus);
                    } else {
                        orderStatus.setCode(errorResponse.getCode());
                        orderStatus.setName(errorResponse.name());
                        orderCanonical.setOrderStatus(orderStatus);
                    }

                    return orderCanonical;
                })
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.getCode(),
                                Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.name())
                )
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error in inkatracker call {} ",e.getMessage());
                    OrderCanonical orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(errorResponse.getCode());
                    orderStatus.setName(errorResponse.name());
                    orderStatus.setDetail(e.getMessage());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                });

    }

    @Override
    public Mono<Void> sendOrderToTracker(OrderCanonical orderCanonical) {
        log.info("[START] sendOrderToTracker - orderCanonical:{}",orderCanonical);

        return null;
    }


}
