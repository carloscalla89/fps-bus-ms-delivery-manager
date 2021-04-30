package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.InsinkResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.ResponseDispatcherCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.dispatcher.StatusDispatcher;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.dto.ecommerce.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.notification.MessageDto;
import com.inretailpharma.digital.deliverymanager.dto.notification.PayloadDto;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.ResponseErrorGeneric;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AdapterAbstractUtil {

    @Autowired
    private OrderTransaction orderTransaction;

    @Autowired
    private ObjectToMapper objectToMapper;

    @Autowired
    private ExternalServicesProperties externalServicesProperties;

    @Autowired
    private ApplicationParameterService applicationParameterService;

    protected String getValueOfParameter(String parameter) {
        return applicationParameterService
                .getApplicationParameterByCodeIs(parameter)
                .getValue();
    }

    protected MessageDto getDtoToNotification(IOrderFulfillment iOrderFulfillment, String statusToSend, String expiredDate,
                                              String localType) {

        MessageDto messageDto = new MessageDto();
        messageDto.setOrderId(iOrderFulfillment.getEcommerceId());
        messageDto.setBrand(iOrderFulfillment.getCompanyCode());
        messageDto.setChannel(iOrderFulfillment.getSource());
        messageDto.setDeliveryTypeCode(iOrderFulfillment.getServiceTypeShortCode());
        messageDto.setLocalType(localType);
        messageDto.setOrderStatus(statusToSend);
        messageDto.setPhoneNumber(iOrderFulfillment.getPhone());
        messageDto.setStatusDate(DateUtils.getCurrentDateMillis());

        PayloadDto payloadDto = new PayloadDto();
        payloadDto.setClientName(iOrderFulfillment.getFirstName());
        payloadDto.setExpirationDate(expiredDate);
        payloadDto.setLocalCode(iOrderFulfillment.getCenterCode());

        messageDto.setPayload(payloadDto);

        log.info("messageDto to notification:{}",messageDto);

        return messageDto;

    }

    protected OrderDto getMappOrder(Long ecommercePurchaseId, StoreCenterCanonical storeCenterCanonical) {

        IOrderFulfillment iOrderFulfillment = orderTransaction.getOrderByecommerceId(ecommercePurchaseId);

        return objectToMapper.orderFulfillmentToOrderDto(
                iOrderFulfillment,
                orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId()),
                storeCenterCanonical
        );
    }

    protected OrderCanonical getOrderFromIOrdersProjects(Long ecommercePurchaseId) {

        IOrderFulfillment iOrderFulfillment = getOrderByEcommerceId(ecommercePurchaseId);

        return objectToMapper
                .getOrderFromIOrdersProjects(
                        iOrderFulfillment,
                        orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId())
                );

    }

    protected IOrderFulfillment getOrderByEcommerceId(Long ecommercePurchaseId) {
        return orderTransaction.getOrderByecommerceId(ecommercePurchaseId);
    }

    protected List<IOrderItemFulfillment> getItemsByOrderId(Long orderId) {
        return orderTransaction.getOrderItemByOrderFulfillmentId(orderId);
    }



    protected String uriRetryDDinka() {
        return externalServicesProperties.getDispatcherLegacySystemUri();
    }

    protected String uriRetryDDmifa() {
        return externalServicesProperties.getDispatcherOrderEcommerceUriMifarma();
    }

    protected String uriGetFillinka() {
        return externalServicesProperties.getDispatcherOrderEcommerceUri();
    }

    protected String uriGetFillmifa() {
        return externalServicesProperties.getDispatcherOrderEcommerceUriMifarma();
    }

    protected String connectTimeOutRetryDD() {
        return externalServicesProperties.getDispatcherLegacySystemConnectTimeout();
    }

    protected String readTimeOutRetryDD() {
        return externalServicesProperties.getDispatcherLegacySystemReadTimeout();
    }

    protected String connectTimeOutGetFillDD() {
        return externalServicesProperties.getDispatcherLegacySystemConnectTimeout();
    }

    protected String readTimeOutGetFillDD() {
        return externalServicesProperties.getDispatcherLegacySystemReadTimeout();
    }

    protected AuditHistoryDto getAuditHistoryDtoFromObject(OrderCanonical orderAudit, String updateBy) {

        return objectToMapper.getAuditHistoryDtoFromObject(orderAudit, updateBy);

    }

    protected List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderId) {
        return orderTransaction.getOrderItemByOrderFulfillmentId(orderId);
    }

    protected ClientHttpConnector generateClientConnector(int connectionTimeOut, long readTimeOut) {
        log.info("generateClientConnector, connectionTimeOut:{}, readTimeOut:{}",connectionTimeOut,readTimeOut);
        HttpClient httpClient = HttpClient.create()
                .tcpConfiguration(tcpClient -> {
                    tcpClient = tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeOut);
                    tcpClient = tcpClient.doOnConnected(conn -> conn
                            .addHandlerLast(
                                    new ReadTimeoutHandler(readTimeOut, TimeUnit.MILLISECONDS))
                    );
                    return tcpClient;
                });



        return new ReactorClientHttpConnector(httpClient);

    }

    protected Mono<OrderCanonical> mapResponseFromDispatcher(ClientResponse clientResponse, Long ecommerceId, String companyCode) {

        if (clientResponse.statusCode().is2xxSuccessful()) {

            return clientResponse
                    .bodyToMono(ResponseDispatcherCanonical.class)
                    .flatMap(cr -> {
                        InsinkResponseCanonical dispatcherResponse = cr.getBody();
                        StatusDispatcher statusDispatcher = cr.getStatus();

                        log.info("result dispatcher to reattempt - body:{}, status:{}",dispatcherResponse, statusDispatcher);

                        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                        Constant.OrderStatus orderStatusUtil = Constant
                                .OrderStatus
                                .getByName(Constant.StatusDispatcherResult.getByName(statusDispatcher.getCode()).getStatus());

                        orderStatus.setCode(orderStatusUtil.getCode());
                        orderStatus.setName(orderStatusUtil.name());
                        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
                        orderStatus.setDetail(dispatcherResponse.getMessageDetail());

                        OrderCanonical resultCanonical = new OrderCanonical();
                        resultCanonical.setEcommerceId(ecommerceId);
                        resultCanonical.setExternalId(
                                Optional
                                        .ofNullable(dispatcherResponse.getInkaventaId())
                                        .map(Long::parseLong).orElse(null)
                        );
                        resultCanonical.setCompanyCode(companyCode);
                        resultCanonical.setOrderStatus(orderStatus);

                        return Mono.just(resultCanonical);

                    });

        } else {
            ResponseErrorGeneric<OrderCanonical> responseErrorGeneric = new ResponseErrorGeneric<>();

            return responseErrorGeneric.getErrorFromClientResponse(clientResponse);
        }

    }

    protected Mono<OrderCanonical> mapResponseErrorFromDispatcher(Throwable e, Long ecommerceId) {

        OrderCanonical orderCanonical = new OrderCanonical();

        orderCanonical.setEcommerceId(ecommerceId);
        OrderStatusCanonical orderStatus = new OrderStatusCanonical();

        orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        orderStatus.setName(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());
        orderStatus.setDetail(e.getMessage());
        orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());

        orderCanonical.setOrderStatus(orderStatus);

        return Mono.just(orderCanonical);
    }

}
