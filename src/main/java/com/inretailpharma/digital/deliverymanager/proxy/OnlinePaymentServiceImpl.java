package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.onlinepayment.OnlinePaymentOrder;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Slf4j
@Service("onlinePayment")
public class OnlinePaymentServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    public OnlinePaymentServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public Mono<OrderCanonical> getResultfromOnlinePaymentExternalServices(Long ecommercePurchaseId, ActionDto actionDto) {

        OnlinePaymentOrder onlinePaymentOrder = new OnlinePaymentOrder();
        onlinePaymentOrder.setEcommerceExternalId(String.valueOf(ecommercePurchaseId));

        String onlinePaymentUri = StringUtils.EMPTY;
        switch (actionDto.getAction()) {
            case Constant.ActionName.LIQUIDATED_ONLINE_PAYMENT:
                onlinePaymentUri = externalServicesProperties.getOnlinePaymentLiquidatedUri();
                break;
            case Constant.ActionName.REJECTED_ONLINE_PAYMENT:
                onlinePaymentUri = externalServicesProperties.getOnlinePaymentLiquidatedUri();
                break;
            default:
        }

        log.info("ecommercePurchaseId::{}, url:{}", ecommercePurchaseId, onlinePaymentUri);
        HttpClient httpClient = HttpClient
                .create()
                .tcpConfiguration(client ->
                        client
                                .option(
                                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                        Integer.parseInt(externalServicesProperties.getOnlinePaymentLiquidatedConnectTimeOut()))
                                .doOnConnected(conn ->
                                        conn
                                                .addHandlerLast(
                                                        new ReadTimeoutHandler(Integer.parseInt(externalServicesProperties.getOnlinePaymentLiquidatedReadTimeOut())))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(Integer.parseInt(externalServicesProperties.getOnlinePaymentLiquidatedReadTimeOut())))
                                )
                );

        return     WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(onlinePaymentUri)
                .build()
                .post()
                .body(Mono.just(onlinePaymentOrder), OnlinePaymentOrder.class)
                .retrieve()
                .bodyToMono(OnlinePaymentOrder.class)
                .map(r -> {
                    log.info("response:{}", r);

                    OrderCanonical orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.getByName(actionDto.getAction()).getCode());
                    orderStatus.setName(Constant.OrderStatus.getByName(actionDto.getAction()).name());
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
                    orderCanonical.setOrderStatus(orderStatus);

                    return orderCanonical;
                })
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommercePurchaseId,
                                Constant.OrderStatus.EMPTY_RESULT_ONLINE_PAYMENT.getCode(),
                                Constant.OrderStatus.EMPTY_RESULT_ONLINE_PAYMENT.name())
                )
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error in inkatrackerlite call {} ",e.getMessage());
                    OrderCanonical orderCanonical = new OrderCanonical();

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.EMPTY_RESULT_ONLINE_PAYMENT.getCode());
                    orderStatus.setName(Constant.OrderStatus.EMPTY_RESULT_ONLINE_PAYMENT.name());
                    orderStatus.setDetail(e.getMessage());
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                });
    }
}
