package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
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

@Slf4j
@Service("inkatrackerlite")
public class InkatrackerLiteServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    public InkatrackerLiteServiceImpl(ExternalServicesProperties externalServicesProperties) {
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
    public Mono<OrderCanonical> sendOrderToTracker(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        log.info("[START] connect inkatracker-lite   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        String actionInkatrackerLite;
        Constant.OrderStatus errorResponse;
        Constant.OrderStatus successResponse;

        String inkatrackerLiteUri;

        switch (actionDto.getAction()) {

            case Constant.ActionName.RELEASE_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_BILLING;
                successResponse = Constant.OrderStatus.RELEASED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_RELEASE_ORDER;

                inkatrackerLiteUri = externalServicesProperties.getInkatrackerLiteUpdateOrderUri();

                break;
            case Constant.ActionName.CANCEL_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.CANCELLED;
                successResponse = Constant.OrderStatus.CANCELLED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_TO_CANCEL_ORDER;

                inkatrackerLiteUri = externalServicesProperties.getInkatrackerLiteUpdateOrderUri();

                break;
            case Constant.ActionName.DELIVER_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.DELIVERED;
                successResponse = Constant.OrderStatus.DELIVERED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_DELIVER;

                inkatrackerLiteUri = externalServicesProperties.getInkatrackerLiteUpdateOrderUri();

                break;
            case Constant.ActionName.READY_PICKUP_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_PICKUP;
                successResponse = Constant.OrderStatus.READY_PICKUP_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_PICKUP;

                inkatrackerLiteUri = externalServicesProperties.getInkatrackerLiteUpdateOrderUri();

                break;
            default:
                actionInkatrackerLite = Constant.OrderStatus.NOT_FOUND_ACTION.name();
                successResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;
                errorResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;

                inkatrackerLiteUri = externalServicesProperties.getInkatrackerLiteUpdateOrderUri();
        }

        log.info("url inkatracket-lite:{}",inkatrackerLiteUri);
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
                .patch()
                .uri(builder ->
                        builder
                                .path("/{orderExternalId}")
                                .queryParam("action",actionInkatrackerLite)
                                .queryParam("idCancellationReason",actionDto.getOrderCancelCode())
                                .build(ecommerceId))
                .retrieve()
                .bodyToMono(OrderInfoCanonical.class)
                .map(r -> {
                    log.info("response:{}", r);
                    OrderCanonical orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(successResponse.getCode());
                    orderStatus.setName(successResponse.name());
                    orderCanonical.setOrderStatus(orderStatus);

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
                    log.error("Error in inkatrackerlite call {} ",e.getMessage());
                    OrderCanonical orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(errorResponse.getCode());
                    orderStatus.setName(errorResponse.name());
                    orderStatus.setDetail(e.getMessage());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                });
    }
}
