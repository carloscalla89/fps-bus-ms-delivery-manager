package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.*;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.generic.ActionWrapper;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.EcommerceMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service("deliveryDispatcherInka")
public class DeliveryDispatcherServiceImpl extends AbstractOrderService implements OrderExternalService{

    private ApplicationParameterService applicationParameterService;
    private ExternalServicesProperties externalServicesProperties;
    private EcommerceMapper ecommerceMapper;

    public DeliveryDispatcherServiceImpl(ApplicationParameterService applicationParameterService,
                                         ExternalServicesProperties externalServicesProperties,
                                         EcommerceMapper ecommerceMapper) {

        this.applicationParameterService = applicationParameterService;
        this.externalServicesProperties = externalServicesProperties;
        this.ecommerceMapper = ecommerceMapper;
    }


    @Override
    public Mono<OrderCanonical> getResultfromSellerExternalServices(OrderInfoCanonical orderInfoCanonical) {

        HttpClient httpClient = HttpClient
                .create()
                .tcpConfiguration(client ->
                        client
                                .option(
                                        ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                        Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderConnectTimeOut()))
                                .doOnConnected(conn ->
                                        conn
                                                .addHandlerLast(
                                                        new ReadTimeoutHandler(Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderReadTimeOut())))
                                                .addHandlerLast(
                                                        new WriteTimeoutHandler(Integer.parseInt(externalServicesProperties.getInkatrackerCreateOrderReadTimeOut())))
                                )
                );



        String inkatrackerUri = externalServicesProperties.getInkatrackerCreateOrderUri();

        log.info("url dispatcher:{} - ecommerceId:{}",inkatrackerUri, orderInfoCanonical.getOrderExternalId());
        return     WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(inkatrackerUri)
                .build()
                .post()
                .body(Mono.just(orderInfoCanonical), OrderInfoCanonical.class)
                .retrieve()
                .bodyToMono(OrderInfoCanonical.class)
                //.timeout(Duration.ofMillis(100))
                .subscribeOn(Schedulers.parallel())
                .map(order -> {

                    TrackerResponseDto r = new TrackerResponseDto();
                    r.setId(order.getOrderExternalId());

                    log.info("reattempt to tracker response:{}",r);

                    OrderCanonical resultCanonical = new OrderCanonical();

                    resultCanonical.setEcommerceId(order.getOrderExternalId());
                    resultCanonical.setTrackerId(order.getOrderExternalId());

                    Constant.OrderStatus orderStatusUtil = Optional.ofNullable(order.getOrderExternalId())
                            .map(s ->
                                    Optional
                                            .ofNullable(r.getCode())
                                            .map(Constant.OrderStatus::getByCode)
                                            .orElse(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS)
                            )
                            .orElseGet(() ->
                                    Constant.OrderStatus.getByCode(
                                            Optional.ofNullable(r.getCode())
                                                    .orElse(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode())
                                    )
                            );

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                    orderStatus.setCode(orderStatusUtil.getCode());
                    orderStatus.setName(orderStatusUtil.name());
                    orderStatus.setDetail(r.getDetail());

                    resultCanonical.setOrderStatus(orderStatus);

                    return resultCanonical;


                })
                .defaultIfEmpty(
                        new OrderCanonical(
                                orderInfoCanonical.getOrderExternalId(),
                                Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                )
                .onErrorResume(e -> {
                    e.printStackTrace();

                    String errorMessage = "General Error invoking '" + inkatrackerUri +
                            "':" + e.getMessage();
                    log.error(errorMessage);
                    OrderCanonical orderCanonical = new OrderCanonical();

                    orderCanonical.setEcommerceId(orderInfoCanonical.getOrderExternalId());

                    OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                    orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                    orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                    orderStatus.setDetail(errorMessage);

                    orderCanonical.setOrderStatus(orderStatus);

                    return Mono.just(orderCanonical);
                });

    }

    @Override
    public Mono<OrderCanonical> sendOrderEcommerce(IOrderFulfillment iOrderFulfillment, List<IOrderItemFulfillment> itemFulfillments,
                                                   String action, StoreCenterCanonical storeCenterCanonical) {
        log.info("send order To sendOrderEcommerce");

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

        String dispatcherUri;

        ApplicationParameter applicationParameter = applicationParameterService
                                                        .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_DD_IKF);


        if (Constant.Logical.getByValueString(applicationParameter.getValue()).value()) {
            dispatcherUri = externalServicesProperties.getDispatcherLegacySystemUri();

            log.info("url dispatcher new activated:{} - company:{}", dispatcherUri, iOrderFulfillment.getCompanyCode());

            return WebClient
                    .builder()
                    .clientConnector(connector)
                    .baseUrl(dispatcherUri)
                    .build()
                    .post()
                    .body(Mono.just(ecommerceMapper.orderFulfillmentToOrderDto(iOrderFulfillment, itemFulfillments, storeCenterCanonical)),
                            OrderDto.class)
                    .exchange()
                    //.bodyToMono(ResponseDispatcherCanonical.class)
                    .flatMap(clientResponse -> {

                        log.info("result dispatcher to reattempt insink and tracker response:{}", clientResponse);



                        if (clientResponse.statusCode().is2xxSuccessful()) {

                            return clientResponse
                                        .bodyToMono(ResponseDispatcherCanonical.class)
                                        .flatMap(cr -> {
                                            InsinkResponseCanonical dispatcherResponse = (InsinkResponseCanonical)cr.getBody();
                                            StatusDispatcher statusDispatcher = (StatusDispatcher)cr.getStatus();

                                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                            Constant.OrderStatus orderStatusUtil = Constant
                                                    .OrderStatus
                                                    .getByName(Constant.StatusDispatcherResult.getByName(statusDispatcher.getCode()).name());

                                            orderStatus.setCode(orderStatusUtil.getCode());
                                            orderStatus.setName(orderStatusUtil.name());
                                            orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                                            Optional.of(statusDispatcher.isSuccessProcess())
                                                    .filter(r -> r)
                                                    .ifPresent(r -> {

                                                        String stringBuffer = "code error:" +
                                                                dispatcherResponse.getErrorCode() +
                                                                ", description:" +
                                                                statusDispatcher.getDescription() +
                                                                ", detail:" +
                                                                dispatcherResponse.getMessageDetail();

                                                        orderStatus.setDetail(stringBuffer);
                                                    });

                                            OrderCanonical resultCanonical = new OrderCanonical();
                                            resultCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                                            resultCanonical.setExternalId(
                                                    Optional
                                                            .ofNullable(dispatcherResponse.getInkaventaId())
                                                            .map(Long::parseLong).orElse(null)
                                            );
                                            resultCanonical.setCompanyCode(iOrderFulfillment.getCompanyCode());
                                            resultCanonical.setOrderStatus(orderStatus);

                                            return Mono.just(resultCanonical);


                                        });

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
                                    iOrderFulfillment.getEcommerceId(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                    )
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        String errorMessage = "Error to invoking'" + dispatcherUri +
                                "':" + e.getMessage();
                        log.error(errorMessage);

                        OrderCanonical orderCanonical = new OrderCanonical();

                        orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                        orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                        orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                        orderStatus.setDetail(errorMessage);
                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                        orderCanonical.setOrderStatus(orderStatus);

                        return Mono.just(orderCanonical);
                    });

        } else {
            dispatcherUri = externalServicesProperties.getDispatcherInsinkTrackerUri();

            log.info("url dispatcher new desactivated:{} - company:{}", dispatcherUri, iOrderFulfillment.getCompanyCode());

            return WebClient
                    .builder()
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl(dispatcherUri)
                    .build()
                    .get()
                    .uri(builder ->
                            builder
                                    .path("/{orderId}")
                                    .queryParam("action", action)
                                    .build(iOrderFulfillment.getEcommerceId()))
                    .retrieve()
                    .bodyToMono(TrackerInsinkResponseCanonical.class)
                    .subscribeOn(Schedulers.parallel())
                    .filter(r -> (r.getInsinkProcess() != null && r.getTrackerProcess() != null))
                    .map(r -> {
                        log.info("result dispatcher to reattempt insink and tracker r:{}", r);
                        OrderCanonical resultCanonical = new OrderCanonical();
                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                        if (r.getInsinkProcess()) {

                            resultCanonical.setExternalId(
                                    Optional
                                            .ofNullable(r.getInsinkResponseCanonical().getInkaventaId())
                                            .map(Long::parseLong).orElse(null)
                            );

                            Constant.OrderStatus orderStatusUtil = Optional.ofNullable(r.getInsinkResponseCanonical().getSuccessCode())
                                    .filter(t -> t.equalsIgnoreCase("0-1") && resultCanonical.getExternalId() == null)
                                    .map(t -> Constant.OrderStatus.SUCCESS_RESERVED_ORDER)
                                    .orElse(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS);

                            orderStatus.setCode(orderStatusUtil.getCode());
                            orderStatus.setName(orderStatusUtil.name());

                        } else {

                            Constant.OrderStatus orderStatusUtil = r.isReleased() ?
                                    Constant.OrderStatus.ERROR_RELEASE_DISPATCHER_ORDER : Constant.OrderStatus.ERROR_INSERT_INKAVENTA;

                            if (r.getInsinkResponseCanonical() != null && r.getInsinkResponseCanonical().getErrorCode() != null &&
                                    r.getInsinkResponseCanonical().getErrorCode().equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)) {
                                orderStatusUtil = Constant.OrderStatus.CANCELLED_ORDER;

                            }

                            orderStatus.setCode(orderStatusUtil.getCode());
                            orderStatus.setName(orderStatusUtil.name());

                            Optional.ofNullable(r.getInsinkResponseCanonical())
                                    .ifPresent(z -> orderStatus.setDetail(z.getMessageDetail()));

                        }

                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
                        resultCanonical.setOrderStatus(orderStatus);
                        resultCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());

                        return resultCanonical;
                    })
                    .defaultIfEmpty(
                            new OrderCanonical(
                                    iOrderFulfillment.getEcommerceId(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                    Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                    )
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        String errorMessage = "Error to invoking'" + dispatcherUri +
                                "':" + e.getMessage();
                        log.error(errorMessage);

                        OrderCanonical orderCanonical = new OrderCanonical();

                        orderCanonical.setEcommerceId(iOrderFulfillment.getEcommerceId());
                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                        orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                        orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                        orderStatus.setDetail(errorMessage);
                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

                        orderCanonical.setOrderStatus(orderStatus);

                        return Mono.just(orderCanonical);
                    });

        }

    }


    private TcpClient configClient(TcpClient client) {

        final int timeout = Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout());
        final int readWriteTimeout = Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout());

        return client.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(600))
                        .addHandlerLast(new WriteTimeoutHandler(600)));
    }

    @Override
    public Mono<OrderCanonical> retrySellerCenterOrder(OrderDto orderDto) {

        String dispatcherUri = externalServicesProperties.getDispatcherRetrySellerCenterUri();

        Long ecommerceId = Long.valueOf(orderDto.getId());
        log.info("url dispatcher:{}",dispatcherUri);
        HttpClient httpClient = HttpClient.create().tcpConfiguration(this::configClient);

        ActionWrapper<OrderDto> actionWrapper = new ActionWrapper<>();
        actionWrapper.setAction(Constant.ActionOrder.ATTEMPT_INSINK_CREATE.name());
        actionWrapper.setBody(orderDto);

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(dispatcherUri)
                .build()
                .put()
                .body(Mono.just(actionWrapper), ActionWrapper.class)
                .retrieve()
                .bodyToMono(TrackerInsinkResponseCanonical.class)
                .subscribeOn(Schedulers.parallel())
                .filter(r -> (r.getInsinkProcess() != null && r.getTrackerProcess() != null))
                .map(this::onRetrySuccess)
                .defaultIfEmpty(emptyOrderCanonical())
                .onErrorResume(e -> Mono.just(onRetryError(e, dispatcherUri)))
                .map(order -> {
                    order.setEcommerceId(ecommerceId);
                    return order;
                });

    }

    private OrderCanonical emptyOrderCanonical() {
        return new OrderCanonical(
                Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name());
    }

    private OrderCanonical onRetrySuccess(TrackerInsinkResponseCanonical response) {
        log.info("result dispatcher to reattempt insink and tracker r:{}",response);
        OrderCanonical resultCanonical = new OrderCanonical();

        if (response.getTrackerProcess() && response.getInsinkProcess()) {

            resultCanonical.setTrackerId(response.getTrackerResponseDto().getId());
            resultCanonical.setExternalId(
                    Optional
                            .ofNullable(response.getInsinkResponseCanonical().getInkaventaId())
                            .map(Long::parseLong).orElse(null)
            );

            Constant.OrderStatus orderStatusUtil = Optional.ofNullable(response.getInsinkResponseCanonical().getSuccessCode())
                    .filter(t -> t.equalsIgnoreCase("0-1") && resultCanonical.getExternalId() == null)
                    .map(t -> Constant.OrderStatus.SUCCESS_RESERVED_ORDER)
                    .orElse(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS);

            OrderStatusCanonical orderStatus = new OrderStatusCanonical();

            orderStatus.setCode(orderStatusUtil.getCode());
            orderStatus.setName(orderStatusUtil.name());

            resultCanonical.setOrderStatus(orderStatus);

        }
        else if (response.getInsinkProcess() && !response.getTrackerProcess()) {
            resultCanonical.setExternalId(
                    Optional
                            .ofNullable(response.getInsinkResponseCanonical().getInkaventaId())
                            .map(Long::parseLong).orElse(null)
            );

            OrderStatusCanonical orderStatus = new OrderStatusCanonical();

            Optional.ofNullable(response.getTrackerResponseDto()).ifPresent(s -> {
                Constant.OrderStatus orderStatusUtil = Constant.OrderStatus.getByCode(s.getCode());

                orderStatus.setCode(orderStatusUtil.getCode());
                orderStatus.setName(orderStatusUtil.name());
                orderStatus.setDetail(s.getDetail());
            });

            orderStatus.setCode(Optional.ofNullable(orderStatus.getCode())
                    .orElse(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode()));
            orderStatus.setDetail(Optional.ofNullable(orderStatus.getDetail())
                    .orElse("Ocurrió un error inesperado al actualizar el inkatracker o inkatracker-lite"));
            resultCanonical.setOrderStatus(orderStatus);

        }
        else {
            Constant.OrderStatus orderStatusUtil = response.isReleased() ?
                    Constant.OrderStatus.ERROR_RELEASE_DISPATCHER_ORDER : Constant.OrderStatus.ERROR_INSERT_INKAVENTA;

            if (response.getInsinkResponseCanonical() != null && response.getInsinkResponseCanonical().getErrorCode() != null &&
                    response.getInsinkResponseCanonical().getErrorCode().equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)) {
                orderStatusUtil = Constant.OrderStatus.CANCELLED_ORDER;

            }

            OrderStatusCanonical orderStatus = new OrderStatusCanonical();

            orderStatus.setCode(orderStatusUtil.getCode());
            orderStatus.setName(orderStatusUtil.name());

            Optional.ofNullable(response.getInsinkResponseCanonical())
                    .ifPresent(z -> orderStatus.setDetail(z.getMessageDetail()));

            resultCanonical.setOrderStatus(orderStatus);
        }

        return resultCanonical;
    }

    private OrderCanonical onRetryError(Throwable exception, String uri) {
        exception.printStackTrace();
        String errorMessage = "Error to invoking'" + uri + "':" + exception.getMessage();
        log.error(errorMessage);

        OrderCanonical orderCanonical = new OrderCanonical();

        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

        orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
        orderStatus.setDetail(errorMessage);

        orderCanonical.setOrderStatus(orderStatus);
        return orderCanonical;
    }
}
