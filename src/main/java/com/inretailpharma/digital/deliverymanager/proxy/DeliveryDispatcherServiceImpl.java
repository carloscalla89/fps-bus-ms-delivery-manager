package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.TrackerInsinkResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.TrackerResponseDto;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service("deliveryDispatcher")
public class DeliveryDispatcherServiceImpl implements OrderExternalService{

    private ExternalServicesProperties externalServicesProperties;

    public DeliveryDispatcherServiceImpl(ExternalServicesProperties externalServicesProperties) {
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
    public Mono<Void> sendOrderToTracker(OrderCanonical orderCanonical) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(Long ecommerceId, ActionDto actionDto, String company) {
        log.info("update order actionOrder.getCode:{}", actionDto.getAction());


        HttpClient httpClient = HttpClient
                                        .create()
                                        .tcpConfiguration(client ->
                                                client
                                                    .option(
                                                            ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                                            Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerConnectTimeout()))
                                                    .doOnConnected(conn ->
                                                            conn
                                                               .addHandlerLast(
                                                                       new ReadTimeoutHandler(Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout())))
                                                               .addHandlerLast(
                                                                       new WriteTimeoutHandler(Integer.parseInt(externalServicesProperties.getDispatcherInsinkTrackerReadTimeout())))
                                                    )
                                        );

        String dispatcherUri;


        switch (Constant.ActionOrder.getByName(actionDto.getAction()).getCode()) {
            case 1:
                // reattempt to send from delivery dispatcher at inkatracker or inkatrackerlite

                if (Constant.Constans.COMPANY_CODE_MF.equals(Optional.ofNullable(company).orElse(Constant.Constans.COMPANY_CODE_IFK))) {
                    dispatcherUri = externalServicesProperties.getDispatcherTrackerUriMifarma();
                } else {
                    dispatcherUri = externalServicesProperties.getDispatcherTrackerUri();
                }

                log.info("url dispatcher:{} - company:{}",dispatcherUri, company);
                return     WebClient
                            .builder()
                            .clientConnector(new ReactorClientHttpConnector(httpClient))
                            .baseUrl(dispatcherUri)
                            .build()
                            .get()
                            .uri(builder ->
                                    builder
                                            .path("/{orderId}")
                                            .queryParam("action",actionDto.getAction())
                                            .build(ecommerceId))
                            .retrieve()
                            .bodyToMono(TrackerResponseDto.class)
                            //.timeout(Duration.ofMillis(100))
                            .subscribeOn(Schedulers.parallel())
                            .map(r -> {

                                OrderCanonical resultCanonical = new OrderCanonical();

                                resultCanonical.setEcommerceId(ecommerceId);
                                resultCanonical.setTrackerId(r.getId());

                                Constant.OrderStatus orderStatusUtil = Optional.ofNullable(r.getId())
                                        .map(s ->
                                                Optional
                                                        .ofNullable(r.getCode())
                                                        .map(Constant.OrderStatus::getByName)
                                                        .orElse(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS)
                                        )
                                        .orElseGet(() ->
                                                Constant.OrderStatus.getByName(
                                                        Optional.ofNullable(r.getCode())
                                                                .orElse(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())
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
                                        ecommerceId,
                                        Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                        Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                            )
                            .onErrorResume(e -> {
                                e.printStackTrace();

                                String errorMessage = "General Error invoking '" + dispatcherUri +
                                        "':" + e.getMessage();
                                log.error(errorMessage);
                                OrderCanonical orderCanonical = new OrderCanonical();

                                orderCanonical.setEcommerceId(ecommerceId);

                                OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                                orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
                                orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
                                orderStatus.setDetail(errorMessage);

                                orderCanonical.setOrderStatus(orderStatus);

                                return Mono.just(orderCanonical);
                            });

            case 2:
                // reattempt to send from delivery dispatcher at insink

                if (Constant.Constans.COMPANY_CODE_MF.equals(Optional.ofNullable(company).orElse(Constant.Constans.COMPANY_CODE_IFK))) {
                    dispatcherUri = externalServicesProperties.getDispatcherInsinkTrackerUriMiFarma();
                } else {
                    dispatcherUri = externalServicesProperties.getDispatcherInsinkTrackerUri();
                }

                log.info("url dispatcher:{} - company:{}",dispatcherUri, company);
                return     WebClient
                        .builder()
                        .clientConnector(new ReactorClientHttpConnector(httpClient))
                        .baseUrl(dispatcherUri)
                        .build()
                        .get()
                        .uri(builder ->
                                builder
                                        .path("/{orderId}")
                                        .queryParam("action",actionDto.getAction())
                                        .build(ecommerceId))
                        .retrieve()
                        .bodyToMono(TrackerInsinkResponseCanonical.class)
                        .subscribeOn(Schedulers.parallel())
                        .filter(r -> (r.getInsinkProcess() != null && r.getTrackerProcess() != null))
                        .map(r -> {
                            log.info("result dispatcher to reattempt insink and tracker r:{}",r);
                            OrderCanonical resultCanonical = new OrderCanonical();

                            if (r.getTrackerProcess() && r.getInsinkProcess()) {

                                resultCanonical.setTrackerId(r.getTrackerResponseDto().getId());
                                resultCanonical.setExternalId(
                                        Optional
                                                .ofNullable(r.getInsinkResponseCanonical().getInkaventaId())
                                                .map(Long::parseLong).orElse(null)
                                );

                                Constant.OrderStatus orderStatusUtil = Optional.ofNullable(r.getInsinkResponseCanonical().getSuccessCode())
                                        .filter(t -> t.equalsIgnoreCase("0-1") && resultCanonical.getExternalId() == null)
                                        .map(t -> Constant.OrderStatus.SUCCESS_RESERVED_ORDER)
                                        .orElse(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS);

                                OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                orderStatus.setCode(orderStatusUtil.getCode());
                                orderStatus.setName(orderStatusUtil.name());

                                resultCanonical.setOrderStatus(orderStatus);

                            } else if (r.getInsinkProcess() && !r.getTrackerProcess()) {
                                resultCanonical.setExternalId(
                                        Optional
                                                .ofNullable(r.getInsinkResponseCanonical().getInkaventaId())
                                                .map(Long::parseLong).orElse(null)
                                );

                                Constant.OrderStatus orderStatusUtil = r.isReleased() ?
                                        Constant.OrderStatus.ERROR_UPDATE_TRACKER_BILLING : Constant.OrderStatus.ERROR_INSERT_TRACKER;

                                OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                orderStatus.setCode(orderStatusUtil.getCode());
                                orderStatus.setName(orderStatusUtil.name());
                                orderStatus.setDetail(r.getTrackerResponseDto().getDetail());

                                resultCanonical.setOrderStatus(orderStatus);

                            } else {

                                Constant.OrderStatus orderStatusUtil = r.isReleased() ?
                                        Constant.OrderStatus.ERROR_RELEASE_ORDER : Constant.OrderStatus.ERROR_INSERT_INKAVENTA;

                                if (r.getInsinkResponseCanonical() != null && r.getInsinkResponseCanonical().getErrorCode() != null &&
                                        r.getInsinkResponseCanonical().getErrorCode().equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)) {
                                    orderStatusUtil = Constant.OrderStatus.CANCELLED_ORDER;

                                }

                                OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                                orderStatus.setCode(orderStatusUtil.getCode());
                                orderStatus.setName(orderStatusUtil.name());

                                Optional.ofNullable(r.getInsinkResponseCanonical())
                                        .ifPresent(z -> orderStatus.setDetail(z.getMessageDetail()));

                                resultCanonical.setOrderStatus(orderStatus);
                            }

                            resultCanonical.setEcommerceId(ecommerceId);

                            return resultCanonical;
                        })
                        .defaultIfEmpty(
                                new OrderCanonical(
                                        ecommerceId,
                                        Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.getCode(),
                                        Constant.OrderStatus.EMPTY_RESULT_DISPATCHER.name())
                        )
                        .onErrorResume(e -> {
                            e.printStackTrace();
                            String errorMessage = "Error to invoking'" + dispatcherUri +
                                    "':" + e.getMessage();
                            log.error(errorMessage);

                            OrderCanonical orderCanonical = new OrderCanonical();

                            orderCanonical.setEcommerceId(ecommerceId);
                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                            orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
                            orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
                            orderStatus.setDetail(errorMessage);

                            orderCanonical.setOrderStatus(orderStatus);

                            return Mono.just(orderCanonical);
                        });

            default:
                OrderCanonical orderCanonical = new OrderCanonical();
                orderCanonical.setEcommerceId(ecommerceId);

                OrderStatusCanonical orderStatus = new OrderStatusCanonical();

                orderStatus.setCode(Constant.OrderStatus.NOT_FOUND_ACTION.getCode());
                orderStatus.setName(Constant.OrderStatus.NOT_FOUND_ACTION.name());

                orderCanonical.setOrderStatus(orderStatus);

                return Mono.just(orderCanonical);

        }

    }

	@Override
	public Mono<Void> assignOrders(ProjectedGroupCanonical projectedGroupCanonical) {
		return null;
	}

	@Override
	public Mono<Void> unassignOrders(UnassignedCanonical unassignedCanonical) {
		return null;
	}
}
