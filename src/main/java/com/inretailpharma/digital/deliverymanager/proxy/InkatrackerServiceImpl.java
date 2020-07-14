package com.inretailpharma.digital.deliverymanager.proxy;

import java.time.ZoneId;
import java.util.Optional;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.DrugstoreCanonical;
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

        Constant.OrderStatus errorResponse;
        Constant.OrderStatus successResponse;

        OrderStatusInkatrackerCanonical orderInkaTrackerStatus = new OrderStatusInkatrackerCanonical();
        switch (actionDto.getAction()) {

            case Constant.ActionName.CANCEL_ORDER:
                orderInkaTrackerStatus.setStatusName(Constant.ActionNameInkatrackerlite.CANCELLED);
                successResponse = Constant.OrderStatus.CANCELLED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_TO_CANCEL_ORDER;
                orderInkaTrackerStatus.setStatusDate(
                        DateUtils.getLocalDateTimeObjectNow().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                );
                orderInkaTrackerStatus.setCode(actionDto.getOrderCancelCode());
                orderInkaTrackerStatus.setCustomNote(actionDto.getOrderCancelObservation());

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
                        Integer.parseInt(externalServicesProperties.getInkatrackerUpdateOrderConnectTimeOut())
                ) // Connection Timeout
                .doOnConnected(connection ->
                        connection.addHandlerLast(
                                new ReadTimeoutHandler(
                                        Integer.parseInt(externalServicesProperties.getInkatrackerUpdateOrderReadTimeOut())
                                )
                        )
                ); // Read Timeout
        log.info("body:{}",orderInkatrackerCanonical);
        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                .baseUrl(externalServicesProperties.getInkatrackerUpdateOrderUri())
                .build()
                .post()
                .body(Mono.just(orderInkatrackerCanonical), OrderInkatrackerCanonical.class)
                .exchange()
                .map(r -> {
                    log.info("response:{}", r.statusCode());

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
    public Mono<OrderCanonical> sendOrderToTracker(OrderCanonical orderCanonicalresult) {
        log.info("[START] sendOrderToTracker - orderCanonical:{}",orderCanonicalresult);

        return centerCompanyService.getExternalInfo(orderCanonicalresult.getLocalCode())
                .zipWith(Mono.just(objectToMapper.convertOrderToOrderInkatrackerCanonical(orderCanonicalresult)), (a,b) -> {
                    b.setDrugstore(
                        new DrugstoreCanonical(a.getLegacyId(), a.getName(), a.getDescription(), a.getAddress(),
                                a.getLatitude().doubleValue(), a.getLongitude().doubleValue(), 0)
                );

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
                            orderCanonical.setEcommerceId(orderCanonicalresult.getEcommerceId());
                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                            if (r.statusCode().is2xxSuccessful()) {
                                orderCanonical.setTrackerId(orderCanonicalresult.getEcommerceId());
                                orderStatus.setCode(Constant.OrderStatus.CONFIRMED.getCode());
                                orderStatus.setName(Constant.OrderStatus.CONFIRMED.name());
                                orderCanonical.setOrderStatus(orderStatus);
                            } else {
                                orderStatus.setCode(Constant.OrderStatus.ERROR_CONFIRMED.getCode());
                                orderStatus.setName(Constant.OrderStatus.ERROR_CONFIRMED.name());
                                orderStatus.setDetail(r.statusCode().getReasonPhrase());
                                orderCanonical.setOrderStatus(orderStatus);
                            }
                            log.info("orderCanonical RESPONSE from inkatracker:{}",orderCanonical);
                            return orderCanonical;
                        })
                        .defaultIfEmpty(
                                new OrderCanonical(
                                        orderCanonicalresult.getEcommerceId(),
                                        Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.getCode(),
                                        Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.name())
                        )
                        .onErrorResume(e -> {
                            e.printStackTrace();
                            log.error("Error in inkatracker call {} ",e.getMessage());
                            OrderCanonical orderCanonical = new OrderCanonical();

                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                            orderStatus.setCode(Constant.OrderStatus.ERROR_CONFIRMED.getCode());
                            orderStatus.setName(Constant.OrderStatus.ERROR_CONFIRMED.name());
                            orderStatus.setDetail(e.getMessage());

                            orderCanonical.setOrderStatus(orderStatus);

                            return Mono.just(orderCanonical);
                        });

            }).flatMap(r -> r);
    }
}
