package com.inretailpharma.digital.deliverymanager.proxy;

import java.time.ZoneId;
import java.util.Optional;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.DrugstoreCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderTrackerCanonical;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderStatusInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Slf4j
@Service("inkatracker")
public class InkatrackerServiceImpl extends AbstractOrderService implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;
    private ApplicationParameterService applicationParameterService;
    private CenterCompanyService centerCompanyService;
    private ObjectToMapper objectToMapper;

    public InkatrackerServiceImpl(ExternalServicesProperties externalServicesProperties,
                                  ApplicationParameterService applicationParameterService,
                                  CenterCompanyService centerCompanyService,
                                  ObjectToMapper objectToMapper) {

        this.externalServicesProperties = externalServicesProperties;
        this.applicationParameterService = applicationParameterService;
        this.centerCompanyService = centerCompanyService;
        this.objectToMapper = objectToMapper;
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

        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        OrderTrackerCanonical orderTrackerCanonical = new OrderTrackerCanonical();
        orderTrackerCanonical.setInkaDeliveryId(actionDto.getExternalBillingId());
        orderTrackerCanonical.setCancelCode(actionDto.getOrderCancelCode());
        orderTrackerCanonical.setCancelClientReason(actionDto.getOrderCancelClientReason());
        orderTrackerCanonical.setCancelReason(actionDto.getOrderCancelReason());
        orderTrackerCanonical.setCancelObservation(actionDto.getOrderCancelObservation());

        log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerUpdateStatusOrderUri());

        TcpClient tcpClient = TcpClient
                    .create()
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                            Integer.parseInt(externalServicesProperties.getInkatrackerUpdateStatusOrderConnectTimeOut())
                    ) // Connection Timeout
                    .doOnConnected(connection ->
                            connection.addHandlerLast(
                                    new ReadTimeoutHandler(
                                            Integer.parseInt(externalServicesProperties.getInkatrackerUpdateOrderReadTimeOut())
                                    )
                            )
                    ); // Read Timeout

        log.info("body:{}",orderTrackerCanonical);

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(externalServicesProperties.getInkatrackerUpdateStatusOrderUri())
                .build()
                .patch()
                .uri(builder -> builder
                                    .path("/{orderExternalId}")
                                    .queryParam("action",actionDto.getAction())
                                    .build(ecommerceId))
                                    .body(Mono.just(orderTrackerCanonical), OrderTrackerCanonical.class)
                                    .exchange()
                                    .map(r -> {
                                        log.info("response:{}", r.statusCode());

                                        OrderCanonical orderCanonical = new OrderCanonical();

                                        Constant.OrderStatus os;
                                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                        if (r.statusCode().is2xxSuccessful()) {

                                            os = Constant.OrderStatus.getByCode(action.getOrderSuccessStatusCode());

                                        } else {

                                            os = Constant.OrderStatus.getByCode(action.getOrderErrorStatusCode());
                                            orderStatus.setDetail(r.statusCode().getReasonPhrase());
                                        }

                                        orderStatus.setCode(os.getCode());
                                        orderStatus.setName(os.name());
                                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                                        orderCanonical.setOrderStatus(orderStatus);

                                        log.info("orderCanonical:{}",orderCanonical);

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

                                        Constant.OrderStatus os = Constant.OrderStatus.getByCode(action.getOrderErrorStatusCode());

                                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                        orderStatus.setCode(os.getCode());
                                        orderStatus.setName(os.name());
                                        orderStatus.setDetail(e.getMessage());
                                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                                        OrderCanonical orderCanonical = new OrderCanonical();
                                        orderCanonical.setOrderStatus(orderStatus);

                                        return Mono.just(orderCanonical);
                                    });

    }

    @Override
    public Mono<OrderCanonical> sendOrderToTracker(OrderCanonical oc) {
        log.info("[START] sendOrderToTracker - orderCanonical:{}",oc);

        return Mono
                .just(objectToMapper.convertOrderToOrderInkatrackerCanonical(oc))
                .flatMap(b -> {

                log.info("Order prepared to send inkatracker - orderInkatracker:{}",b);

                log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerCreateOrderUri());

                TcpClient tcpClient = TcpClient
                        .create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderConnectTimeOut())
                        ) // Connection Timeout
                        .doOnConnected(connection ->
                                connection.addHandlerLast(
                                        new ReadTimeoutHandler(
                                                Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderReadTimeOut())
                                        )
                                )
                        ); // Read Timeout

                return WebClient
                        .builder()
                        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                        .baseUrl(externalServicesProperties.getInkatrackerCreateOrderUri())
                        .build()
                        .post()
                        .body(Mono.just(b), OrderInkatrackerCanonical.class)
                        .exchange()
                        .map(r -> {
                            log.info("response:{}", r.statusCode());

                            OrderCanonical orderCanonical = new OrderCanonical();
                            orderCanonical.setEcommerceId(oc.getEcommerceId());
                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                            if (r.statusCode().is2xxSuccessful()) {
                                orderCanonical.setTrackerId(oc.getEcommerceId());
                                orderStatus.setCode(Constant.OrderStatus.CONFIRMED.getCode());
                                orderStatus.setName(Constant.OrderStatus.CONFIRMED.name());

                            } else {
                                orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                                orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                                orderStatus.setDetail(r.statusCode().getReasonPhrase());

                            }
                            orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
                            orderCanonical.setOrderStatus(orderStatus);
                            log.info("orderCanonical RESPONSE from inkatracker:{}",orderCanonical);
                            return orderCanonical;
                        })
                        .defaultIfEmpty(
                                new OrderCanonical(
                                        oc.getEcommerceId(),
                                        Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode(),
                                        Constant.OrderStatus.ERROR_INSERT_TRACKER.name())
                        )
                        .onErrorResume(e -> {
                            e.printStackTrace();
                            log.error("Error in inkatracker call {} ",e.getMessage());
                            OrderCanonical orderCanonical = new OrderCanonical();

                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                            orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                            orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                            orderStatus.setDetail(e.getMessage());
                            orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
                            orderCanonical.setOrderStatus(orderStatus);

                            return Mono.just(orderCanonical);
                        });

            });
    }
}
