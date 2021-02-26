package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.adapter.AdapterInterface;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.*;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.inretailpharma.digital.deliverymanager.util.Constant.OrderStatus.SUCCESS_RESULT_ONLINE_PAYMENT;

@Slf4j
@Component("proxy")
public class OrderFacadeProxyImpl implements OrderFacadeProxy{

    private OrderCancellationService orderCancellationService;

    private OrderTransaction orderTransaction;

    private AdapterInterface adapterTrackerInterface;

    private AdapterInterface adapterAuditInterface;

    private AdapterInterface adapterNotificationInterface;

    private OrderExternalService externalStoreService;

    private OrderExternalService externalOnlinePaymentService;

    private final ApplicationContext context;

    @Autowired
    public OrderFacadeProxyImpl(OrderCancellationService orderCancellationService, OrderTransaction orderTransaction,
                                @Qualifier("trackeradapter") AdapterInterface adapterTrackerInterface,
                                @Qualifier("auditadapter") AdapterInterface adapterAuditInterface,
                                @Qualifier("notificationadapter") AdapterInterface adapterNotificationInterface,
                                @Qualifier("store") OrderExternalService externalStoreService,
                                @Qualifier("onlinePayment") OrderExternalService externalOnlinePaymentService,
                                ApplicationContext context) {

        this.orderCancellationService = orderCancellationService;
        this.orderTransaction = orderTransaction;
        this.adapterTrackerInterface = adapterTrackerInterface;
        this.adapterAuditInterface = adapterAuditInterface;
        this.adapterNotificationInterface = adapterNotificationInterface;
        this.externalStoreService = externalStoreService;
        this.externalOnlinePaymentService = externalOnlinePaymentService;
        this.context = context;
    }

    @Override
    public Mono<OrderCanonical> sendOrderToTracker(Long orderId, Long ecommerceId, Long externalId, String serviceTypeCode,
                                                   String statusDetail, String statusName, String orderCancelCode,
                                                   String orderCancelObservation, String companyCode, String localCode,
                                                   boolean sendNewAudit) {

        UtilClass utilClass = new UtilClass(serviceTypeCode);


        return externalStoreService
                    .getStoreByCompanyCodeAndLocalCode(companyCode, localCode)
                    .flatMap(resultStore -> adapterTrackerInterface
                                                .sendOrderTracker(
                                                        ((OrderExternalService)context.getBean(utilClass.getClassToTracker())),
                                                        resultStore,
                                                        ecommerceId,
                                                        externalId,
                                                        statusDetail,
                                                        statusName,
                                                        orderCancelCode,
                                                        orderCancelObservation,
                                                        null
                                                ).flatMap(responses -> getOrderResponse(
                                                        responses,
                                                        orderId,
                                                        ecommerceId,
                                                        externalId,
                                                        orderCancelCode,
                                                        orderCancelObservation,
                                                        null,
                                                        sendNewAudit
                                                        )
                                                )
                    );

    }

    @Override
    public Mono<OrderCanonical> sendToUpdateOrder(Long orderId, Long ecommerceId, Long externalId, ActionDto actionDto,
                                                  String serviceType, String serviceTypeCode, String source, String channel,
                                                  String companyCode, String localCode, String statusCode, String clientName,
                                                  String phone, boolean sendNewFlow, boolean sendNotificationByChannel,
                                                  boolean sendNotificationByStatus) {

        log.info("sendToUpdateOrder proxy: orderId:{}, ecommerceId:{}, action:{}, sendNewFlow:{}, serviceType:{}, " +
                 "serviceTypeCode:{}, sendNotificationByChannel:{}, sendNotificationByStatus:{}", orderId, ecommerceId,
                actionDto, sendNewFlow, serviceType, serviceTypeCode, sendNotificationByChannel, sendNotificationByStatus);

        CancellationCodeReason codeReason;

        UtilClass utilClass = new UtilClass(serviceTypeCode,serviceType, actionDto.getAction(), actionDto.getOrigin(),
                                            statusCode, sendNewFlow);

        Function<List<OrderCanonical>,Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(ecommerceId, actionDto, serviceType, serviceTypeCode,
                        source, channel, companyCode, localCode, statusCode, clientName, phone, sendNotificationByChannel,
                        sendNotificationByStatus);

        Function<OrderCanonical,Mono<? extends Boolean>> publisherNotificationSingle =
                responses -> processSendNotification(ecommerceId, actionDto, serviceType, serviceTypeCode,
                        source, channel, companyCode, localCode, statusCode, clientName, phone, sendNotificationByChannel,
                        sendNotificationByStatus);

        if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

            if (source.equalsIgnoreCase(Constant.Source.SC.name())) {

                OrderExternalService sellerCenterService = (OrderExternalService) context.getBean(
                            Constant.SellerCenter.BEAN_SERVICE_NAME
                );

                log.info("[START] Add controversy because of order cancellation comming from seller center");

                ControversyRequestDto controversyRequestDto = new ControversyRequestDto();
                controversyRequestDto.setDate(DateUtils.getLocalDateTimeNow());
                controversyRequestDto.setText(actionDto.getOrderCancelObservation() != null ? actionDto.getOrderCancelObservation() : "");
                controversyRequestDto.setType(Constant.SellerCenter.ControversyTypes.CT.getType());

                sellerCenterService.addControversy(controversyRequestDto, ecommerceId).subscribe();

                    log.info("[END] add controversy");
            }

            if (actionDto.getOrderCancelCode() != null && actionDto.getOrderCancelAppType() != null) {
                codeReason = orderCancellationService
                            .geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType());
            } else {
                codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
            }

            // Esta condición está para saber si una orden se manda a cancelar pero no está en el tracker porque de seguro falló al momento de registrarse

            if (Constant.OrderStatus.getByCode(statusCode).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name())
                    || Constant.OrderStatus.getByCode(statusCode).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())) {


                return externalStoreService
                        .getStoreByCompanyCodeAndLocalCode(companyCode, localCode)
                        .flatMap(resultStore -> adapterTrackerInterface
                                                    .sendOrderTracker(
                                                            ((OrderExternalService)context.getBean(utilClass.getClassToTracker())),
                                                            resultStore,
                                                            ecommerceId,
                                                            externalId,
                                                            null,
                                                            Constant.OrderStatus.CANCELLED_ORDER.name(),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                            actionDto.getOrderCancelObservation(),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null)
                                                    )
                                                    .flatMap(responses -> getOrderResponse(
                                                            responses,
                                                            orderId,
                                                            ecommerceId,
                                                            externalId,
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                            actionDto.getOrderCancelObservation(),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null),
                                                            sendNewFlow
                                                            )
                                                    )
                        )
                        .filter(response -> Constant.OrderStatus.getByName(response.getOrderStatus().getName()).isSuccess())
                        .flatMap(publisherNotificationSingle)
                        .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(ecommerceId,actionDto.getAction(), null))
                        .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(ecommerceId, actionDto.getAction(), Constant.ERROR_PROCESS)));


            } else {
                return Flux
                        .fromIterable(utilClass.getClassesToSend())
                        .flatMap(objectClass ->
                                ((AdapterInterface)context
                                        .getBean(objectClass))
                                        .getResultfromExternalServices(
                                                ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                                ecommerceId,
                                                actionDto,
                                                companyCode,
                                                serviceType,
                                                orderId,
                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                actionDto.getOrderCancelObservation(),
                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null)
                                        )
                        )
                        .flatMap(responses -> getOrderResponse(
                                                    responses,
                                                    orderId,
                                                    ecommerceId,
                                                    externalId,
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                    actionDto.getOrderCancelObservation(),
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null),
                                                    sendNewFlow
                                                )
                        )
                        .buffer()
                        .filter(finalResponse ->
                                finalResponse
                                        .stream()
                                        .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                        )
                        .flatMap(publisherNotification)
                        .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(ecommerceId,actionDto.getAction(), null))
                        .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(ecommerceId, actionDto.getAction(), Constant.ERROR_PROCESS)))
                        .single();
            }

        }

        return Flux
                .fromIterable(utilClass.getClassesToSend())
                .flatMap(objectClass ->
                        ((AdapterInterface)context
                                .getBean(objectClass))
                                .getResultfromExternalServices(
                                        ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                        ecommerceId,
                                        actionDto,
                                        companyCode,
                                        serviceType,
                                        orderId,
                                        null,
                                        null,
                                        null
                                )
                )
                .flatMap(responses -> getOrderResponse(
                        responses,
                        orderId,
                        ecommerceId,
                        externalId,
                        null,
                        null,
                        null,
                        sendNewFlow
                        )
                )
                .buffer()
                .filter(finalResponse ->
                        finalResponse
                                .stream()
                                .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                )
                .flatMap(publisherNotification)
                .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(ecommerceId,actionDto.getAction(), null))
                .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(ecommerceId, actionDto.getAction(), Constant.ERROR_PROCESS)))
                .single();
    }

    @Override
    public Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long id, Long ecommerceId, Long externalId,
                                                  String orderCancelCode, String orderCancelObservation,
                                                  String orderCancelAppType, boolean sendNewAudit) {

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeObjectNow();

        orderCanonical.setEcommerceId(ecommerceId);

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation,
                orderCancelCode, orderCancelAppType,
                orderCanonical.getOrderStatus().getCode(), id,
                localDateTime, Optional.ofNullable(orderCancelCode).map(oc -> localDateTime).orElse(null)
        );

        orderCanonical.setExternalId(externalId);
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        if (!orderCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
            adapterAuditInterface.updateExternalAudit(sendNewAudit, orderCanonical).subscribe();
        }

        return Mono.just(orderCanonical);

    }

    @Override
    public void createExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical) {

        adapterAuditInterface.createExternalAudit(sendNewAudit, orderAuditCanonical).subscribe();
    }

    @Override
    public Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localcode) {
        return externalStoreService.getStoreByCompanyCodeAndLocalCode(companyCode, localcode);
    }

    @Override
    public Mono<OrderCanonical> getfromOnlinePaymentExternalServices(Long orderId, Long ecommercePurchaseId, String source,
                                                                     String serviceTypeShortCode, String companyCode,
                                                                     ActionDto actionDto) {

        return externalOnlinePaymentService
                .getResultfromOnlinePaymentExternalServices(ecommercePurchaseId, source, serviceTypeShortCode, companyCode, actionDto)
                .map(r -> {
                    log.info("[START] to update online payment order = {}", r);

                    if(SUCCESS_RESULT_ONLINE_PAYMENT.getCode().equals(r.getOrderStatus().getCode())) {
                        Constant.OrderStatus status = Constant.OrderStatus.getByName(actionDto.getAction());
                        OrderStatusCanonical paymentRsp = new OrderStatusCanonical();
                        paymentRsp.setCode(status.getCode());
                        paymentRsp.setName(status.name());
                        r.setOrderStatus(paymentRsp);
                        String onlinePaymentStatus = Constant.OnlinePayment.LIQUIDETED;
                        log.info("[PROCESS] to update online payment order::{}, status::{}", orderId, onlinePaymentStatus);
                        orderTransaction.updateOrderOnlinePaymentStatusByExternalId(orderId,onlinePaymentStatus);
                    }
                    log.info("[END] to update order");
                    return r;
                });

    }

    private Mono<Boolean> processSendNotification(Long ecommerceId, ActionDto actionDto, String serviceType,
                                               String serviceTypeCode, String source, String channel, String companyCode,
                                               String localCode, String statusCode, String clientName, String phone,
                                               boolean sendNotificationByChannel, boolean sendNotificationByStatus) {

        if (sendNotificationByChannel && sendNotificationByStatus) {

            return adapterNotificationInterface
                        .sendNotification(channel, serviceTypeCode, statusCode, ecommerceId, companyCode, localCode, null,
                                phone, clientName, null, null, null
                        );
        }

        return Mono.just(true);

    }

}
