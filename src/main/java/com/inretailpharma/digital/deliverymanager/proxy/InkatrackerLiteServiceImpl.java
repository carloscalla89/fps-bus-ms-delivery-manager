package com.inretailpharma.digital.deliverymanager.proxy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.List;

@Slf4j
@Service("inkatrackerlite")
public class InkatrackerLiteServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;
    private ObjectToMapper objectToMapper;

    public InkatrackerLiteServiceImpl(ExternalServicesProperties externalServicesProperties, ObjectToMapper objectToMapper) {
        this.externalServicesProperties = externalServicesProperties;
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
    public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment,
                                                   List<IOrderItemFulfillment> itemFulfillments,
                                                   StoreCenterCanonical storeCenterCanonical,
                                                   Long externalId, String status) {

        return Mono
                .just(objectToMapper.convertOrderToOrderInkatrackerCanonical(iOrderFulfillment, itemFulfillments,
                        storeCenterCanonical, externalId, status)
                )
                .flatMap(b -> {

                    try {
                        log.info("Order prepared to send inkatracker lite:{}",new ObjectMapper().writeValueAsString(b));
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerLiteCreateOrderUri());

                    TcpClient tcpClient = TcpClient
                            .create()
                            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                    Integer.parseInt(externalServicesProperties.getInkatrackerLiteCreateOrderConnectTimeOut())
                            ) // Connection Timeout
                            .doOnConnected(connection ->
                                    connection.addHandlerLast(
                                            new ReadTimeoutHandler(
                                                    Integer.parseInt(externalServicesProperties.getInkatrackerLiteCreateOrderReadTimeOut())
                                            )
                                    )
                            ); // Read Timeout

                    return WebClient
                            .builder()
                            .clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient)))
                            .baseUrl(externalServicesProperties.getInkatrackerLiteCreateOrderUri())
                            .build()
                            .post()
                            .body(Mono.just(b), OrderInkatrackerCanonical.class)
                            .exchange()
                            .flatMap(clientResponse -> {
                                log.info("response lite:{}, phrase:{}", clientResponse.statusCode(),
                                        clientResponse.statusCode().getReasonPhrase());

                                OrderCanonical orderCanonical = new OrderCanonical();
                                orderCanonical.setId(iOrderFulfillment.getOrderId());
                                orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                                orderCanonical.setExternalId(externalId);

                                OrderStatusCanonical orderStatus;

                                if (clientResponse.statusCode().is2xxSuccessful()) {
                                    orderCanonical.setTrackerId(iOrderFulfillment.getEcommerceId());
                                    orderStatus = objectToMapper.getOrderStatusErrorCancel(Constant.OrderStatus.CONFIRMED_TRACKER.getCode(),
                                            null);

                                    orderCanonical.setOrderStatus(orderStatus);

                                    log.info("orderCanonical RESPONSE from inkatracker lite:{}",orderCanonical);
                                    return Mono.just(orderCanonical);

                                } else {

                                    return clientResponse.body(BodyExtractors.toDataBuffers()).reduce(DataBuffer::write).map(dataBuffer -> {
                                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                        dataBuffer.read(bytes);
                                        DataBufferUtils.release(dataBuffer);
                                        return bytes;
                                    })
                                            .defaultIfEmpty(new byte[0])
                                            .flatMap(bodyBytes -> Mono.error(new CustomException(clientResponse.statusCode().value()
                                                    +":"+clientResponse.statusCode().getReasonPhrase()+":"+new String(bodyBytes),
                                                    clientResponse.statusCode().value()))
                                            );
                                }


                            })
                            .defaultIfEmpty(
                                    new OrderCanonical(
                                            iOrderFulfillment.getOrderId(),
                                            iOrderFulfillment.getEcommerceId(),
                                            objectToMapper.getOrderStatusErrorCancel(
                                                    Constant.OrderStatus.EMPTY_RESULT_INKATRACKERLITE.getCode(), "Result inkatracker-lite is empty")
                                    )
                            )
                            .onErrorResume(e -> {
                                e.printStackTrace();
                                log.error("Error in inkatracker LITE call {} ",e.getMessage());

                                OrderCanonical orderCanonical = new OrderCanonical();
                                orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                                orderCanonical.setId(iOrderFulfillment.getOrderId());

                                OrderStatusCanonical orderStatus;

                                if (iOrderFulfillment.getStatusCode().equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER.getCode())) {

                                    orderStatus = objectToMapper.getOrderStatusErrorCancel(Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.getCode(), e.getMessage());

                                } else {

                                    orderStatus = objectToMapper.getOrderStatusErrorCancel(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode(), e.getMessage());

                                }

                                orderCanonical.setOrderStatus(orderStatus);

                                return Mono.just(orderCanonical);
                            });

                }).doOnSuccess((f) ->  log.info("[END] Create Order To Tracker lite- orderCanonical:{}",f));

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
                errorResponse = Constant.OrderStatus.ERROR_RELEASE_DISPATCHER_ORDER;

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

            case Constant.ActionName.PICK_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.PICKING;
                successResponse = Constant.OrderStatus.PICKED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_PICKED;

                inkatrackerLiteUri = externalServicesProperties.getInkatrackerLiteUpdateOrderUri();

                break;

            case Constant.ActionName.PREPARE_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.PREPARED;
                successResponse = Constant.OrderStatus.PREPARED_ORDER;
                errorResponse = Constant.OrderStatus.ERROR_PREPARED;

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
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
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
                    orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                });
    }
}
