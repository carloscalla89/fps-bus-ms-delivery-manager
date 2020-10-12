package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.InvoicedOrderCanonical;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderTrackerCanonical;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderTrackerCanonical;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.InvoicedOrderCanonical;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java.util.Optional;

@Slf4j
@Service("inkatracker")
public class InkatrackerServiceImpl extends AbstractOrderService implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;
    private ObjectToMapper objectToMapper;

    public InkatrackerServiceImpl(ExternalServicesProperties externalServicesProperties,
                                  ObjectToMapper objectToMapper) {

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
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        log.info("[START]  connect inkatracker   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionDto.getAction());

        Constant.ActionOrder action = Constant.ActionOrder.getByName(actionDto.getAction());

        log.info("response action:{}", action);

        OrderTrackerCanonical orderTrackerCanonical = new OrderTrackerCanonical();
        List<InvoicedOrderCanonical> invoicedList = new ArrayList<>();
        if(actionDto.getInvoicedOrderList() != null) {
            actionDto.getInvoicedOrderList().forEach(i -> {
                InvoicedOrderCanonical invoiced = new InvoicedOrderCanonical();
                invoiced.setInvoicedNumber(i.getInvoicedNumber());
                invoicedList.add(invoiced);
            });
        }
        orderTrackerCanonical.setInvoicedList(invoicedList);
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
                                        log.info("response r :{}", r);
                                        log.info("response action :{}", action);
                                        log.info("response:{}", r.statusCode());

                                        OrderCanonical orderCanonical = new OrderCanonical();
                                        orderCanonical.setExternalId(Optional.ofNullable(actionDto.getExternalBillingId()).map(Long::parseLong).orElse(null));

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

                    log.info("url inkatracker:{}",externalServicesProperties.getInkatrackerCreateOrderUri());

                    HttpClient httpClient = HttpClient.create()
                            .tcpConfiguration(tcpClient -> {
                                tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                        Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderConnectTimeOut()));
                                tcpClient = tcpClient.doOnConnected(conn -> conn
                                        .addHandlerLast(new ReadTimeoutHandler(Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderReadTimeOut()), TimeUnit.MILLISECONDS)));
                                return tcpClient;
                            });
                    // create a client http connector using above http client
                    ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

                    return WebClient
                            .builder()
                            .clientConnector(connector)
                            .baseUrl(externalServicesProperties.getInkatrackerCreateOrderUri())
                            .build()
                            .post()
                            .body(Mono.just(b), OrderInkatrackerCanonical.class)
                            .exchange()
                            .flatMap(clientResponse -> {
                                log.info("response:{}", clientResponse.statusCode());

                                OrderCanonical orderCanonical = new OrderCanonical();
                                orderCanonical.setId(iOrderFulfillment.getOrderId());
                                orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                                orderCanonical.setExternalId(externalId);

                                OrderStatusCanonical orderStatus;

                                if (clientResponse.statusCode().is2xxSuccessful()) {

                                    orderCanonical.setTrackerId(iOrderFulfillment.getEcommerceId());
                                    orderStatus = objectToMapper.getOrderStatusInkatracker(status, statusDetail);

                                    orderCanonical.setOrderStatus(orderStatus);

                                    log.info("orderCanonical RESPONSE from inkatracker:{}",orderCanonical);
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
                                            objectToMapper.getOrderStatusInkatracker(
                                                    Constant.OrderStatus.EMPTY_RESULT_INKATRACKER.name(), "Result inkatracker is empty")
                                    )
                            )
                            .onErrorResume(e -> {
                                e.printStackTrace();

                                log.error("Error in inkatracker call {}",e.getMessage());

                                OrderCanonical orderCanonical = new OrderCanonical();
                                orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                                orderCanonical.setId(iOrderFulfillment.getOrderId());

                                OrderStatusCanonical orderStatus;

                                if (iOrderFulfillment.getStatusCode().equalsIgnoreCase(Constant.OrderStatus.CANCELLED_ORDER.getCode())) {

                                    orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.ERROR_TO_CANCEL_ORDER.name(), e.getMessage());

                                } else {

                                    orderStatus = objectToMapper.getOrderStatusInkatracker(Constant.OrderStatus.ERROR_INSERT_TRACKER.name(), e.getMessage());

                                }

                                orderCanonical.setOrderStatus(orderStatus);

                                return Mono.just(orderCanonical);
                            });

                });


    }
}
