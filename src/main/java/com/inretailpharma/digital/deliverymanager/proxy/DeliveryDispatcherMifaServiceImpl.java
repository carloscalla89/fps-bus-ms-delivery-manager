package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.InsinkResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.ResponseDispatcherCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.StatusDispatcher;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.TrackerInsinkResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.generic.ActionWrapper;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
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

@Slf4j
@Service("deliveryDispatcherMifa")
public class DeliveryDispatcherMifaServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ApplicationParameterService applicationParameterService;
    private ExternalServicesProperties externalServicesProperties;
    private EcommerceMapper ecommerceMapper;

    public DeliveryDispatcherMifaServiceImpl(ApplicationParameterService applicationParameterService,
                                             ExternalServicesProperties externalServicesProperties,
                                             EcommerceMapper ecommerceMapper) {
        this.applicationParameterService = applicationParameterService;
        this.externalServicesProperties = externalServicesProperties;
        this.ecommerceMapper = ecommerceMapper;
    }


    @Override
    public Mono<OrderCanonical> sendOrderEcommerce(IOrderFulfillment iOrderFulfillment, List<IOrderItemFulfillment> itemFulfillments,
                                                   String action, StoreCenterCanonical storeCenterCanonical) {

        String dispatcherUri;

        ApplicationParameter applicationParameter = applicationParameterService
                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.ACTIVATED_DD_MF);


        if (Constant.Logical.getByValueString(applicationParameter.getValue()).value()) {
            dispatcherUri = externalServicesProperties.getDispatcherLegacySystemUriMifarma();

            log.info("url dispatcher new activated:{} - company:{}", dispatcherUri, iOrderFulfillment.getCompanyCode());

            return WebClient
                    .builder()
                    .clientConnector(
                            generateClientConnector(
                                    Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerConnectTimeout()),
                                    Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout())
                            )
                    )
                    .baseUrl(dispatcherUri)
                    .build()
                    .post()
                    .body(Mono.just(ecommerceMapper.orderFulfillmentToOrderDto(iOrderFulfillment, itemFulfillments, storeCenterCanonical)), OrderDto.class)
                    .exchange()
                    //.bodyToMono(ResponseDispatcherCanonical.class)
                    .flatMap(clientResponse -> {

                        log.info("result dispatcher to reattempt insink and tracker response:{}", clientResponse);

                        if (clientResponse.statusCode().is2xxSuccessful()) {

                            return clientResponse
                                    .bodyToMono(ResponseDispatcherCanonical.class)
                                    .flatMap(cr -> {
                                        InsinkResponseCanonical dispatcherResponse = cr.getBody();
                                        StatusDispatcher statusDispatcher = cr.getStatus();

                                        log.info("body:{}, status:{}",dispatcherResponse, statusDispatcher);

                                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                        Constant.OrderStatus orderStatusUtil = Constant
                                                .OrderStatus
                                                .getByName(Constant.StatusDispatcherResult.getByName(statusDispatcher.getCode()).getStatus());

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
            dispatcherUri = externalServicesProperties.getDispatcherInsinkTrackerUriMiFarma();

            log.info("url dispatcher new desactivated:{} - company:{}", dispatcherUri, iOrderFulfillment.getCompanyCode());

            return WebClient
                    .builder()
                    .clientConnector(
                            generateClientConnector(
                                    Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerConnectTimeout()),
                                    Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout())
                            )
                    )
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
                                    .orElse(Constant.OrderStatus.CONFIRMED);

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

    @Override
    public Mono<OrderCanonical> retrySellerCenterOrder(OrderDto orderDto) {

        String dispatcherUri = externalServicesProperties.getDispatcherRetrySellerCenterUri();

        Long ecommerceId = Long.valueOf(orderDto.getId());
        log.info("url dispatcher:{}",dispatcherUri);

        ActionWrapper<OrderDto> actionWrapper = new ActionWrapper<>();
        actionWrapper.setAction(Constant.ActionOrder.ATTEMPT_INSINK_CREATE.name());
        actionWrapper.setBody(orderDto);

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                                Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerConnectTimeout()),
                                Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout())
                        )
                )
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

    public Mono<com.inretailpharma.digital.deliverymanager.dto.OrderDto> getOrderFromEcommerce(Long ecommerceId) {
        return null;
    }
}
