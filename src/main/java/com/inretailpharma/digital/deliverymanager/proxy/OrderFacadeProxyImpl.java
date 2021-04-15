package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.adapter.AdapterInterface;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.HistorySynchronizedDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
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

    private ApplicationParameterService applicationParameterService;

    private final ApplicationContext context;

    @Autowired
    public OrderFacadeProxyImpl(OrderCancellationService orderCancellationService, OrderTransaction orderTransaction,
                                @Qualifier("trackeradapter") AdapterInterface adapterTrackerInterface,
                                @Qualifier("auditadapter") AdapterInterface adapterAuditInterface,
                                @Qualifier("notificationadapter") AdapterInterface adapterNotificationInterface,
                                @Qualifier("store") OrderExternalService externalStoreService,
                                @Qualifier("onlinePayment") OrderExternalService externalOnlinePaymentService,
                                ApplicationParameterService applicationParameterService,
                                ApplicationContext context) {

        this.orderCancellationService = orderCancellationService;
        this.orderTransaction = orderTransaction;
        this.adapterTrackerInterface = adapterTrackerInterface;
        this.adapterAuditInterface = adapterAuditInterface;
        this.adapterNotificationInterface = adapterNotificationInterface;
        this.externalStoreService = externalStoreService;
        this.externalOnlinePaymentService = externalOnlinePaymentService;
        this.applicationParameterService = applicationParameterService;
        this.context = context;
    }
    @Override
    public Mono<OrderCanonical> createOrderToTracker(Long orderId, Long ecommerceId, Long externalId,
                                                     String classImplementTracker, String statusDetail, String statusName,
                                                     String orderCancelCode, String orderCancelDescription,
                                                     String orderCancelObservation, StoreCenterCanonical store,
                                                     String source, boolean sendNewAudit, String updateBy) {

        log.info("Send to create the order in tracker when its created for the first time");

        UtilClass utilClass = new UtilClass(classImplementTracker);

        return adapterTrackerInterface
                .sendOrderTracker(
                        ((OrderExternalService)context.getBean(utilClass.getClassToTracker())),
                        store,
                        ecommerceId,
                        externalId,
                        statusDetail,
                        statusName,
                        orderCancelCode,
                        orderCancelDescription,
                        orderCancelObservation
                ).flatMap(responses -> getOrderResponse(
                        responses,
                        orderId,
                        ecommerceId,
                        externalId,
                        orderCancelCode,
                        orderCancelObservation,
                        source,
                        utilClass.getOnlyTargetComponentTracker(),
                        sendNewAudit,
                        updateBy,
                        null
                        )
                );

    }

    @Override
    public Mono<OrderCanonical> sendOrderToTracker(IOrderFulfillment iOrderFulfillment, String statusDetail,
                                                   String statusName, String orderCancelCode, String orderCancelDescription,
                                                   String orderCancelObservation, String updateBy) {
        log.info("Send to create the order in tracker when its sent by reattempt");

        UtilClass utilClass = new UtilClass(iOrderFulfillment.getClassImplement());

        return externalStoreService
                    .getStoreByCompanyCodeAndLocalCode(iOrderFulfillment.getCompanyCode(), iOrderFulfillment.getCenterCode())
                    .flatMap(resultStore -> adapterTrackerInterface
                                                .sendOrderTracker(
                                                        ((OrderExternalService)context.getBean(utilClass.getClassToTracker())),
                                                        resultStore,
                                                        iOrderFulfillment.getEcommerceId(),
                                                        iOrderFulfillment.getExternalId(),
                                                        statusDetail,
                                                        statusName,
                                                        orderCancelCode,
                                                        orderCancelDescription,
                                                        orderCancelObservation
                                                ).flatMap(responses -> getOrderResponse(
                                                        responses,
                                                        iOrderFulfillment.getOrderId(),
                                                        iOrderFulfillment.getEcommerceId(),
                                                        iOrderFulfillment.getExternalId(),
                                                        orderCancelCode,
                                                        orderCancelObservation,
                                                        iOrderFulfillment.getSource(),
                                                        utilClass.getOnlyTargetComponentTracker(),
                                                        iOrderFulfillment.getSendNewFlow(),
                                                        updateBy,
                                                        null
                                                        )
                                                )
                    );

    }

    @Override
    public Mono<OrderCanonical> sendToUpdateOrder(IOrderFulfillment iOrderFulfillment, ActionDto actionDto) {

        log.info("sendToUpdateOrder proxy: orderId:{}, ecommerceId:{}, action:{}, sendNewFlow:{}, serviceType:{}, " +
                 "serviceShortCode:{}, classImplementTracker:{}, source:{}, channel:{}, sendNotificationByChannel:{}",
                iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), actionDto, iOrderFulfillment.getSendNewFlow(),
                iOrderFulfillment.getServiceType(), iOrderFulfillment.getServiceTypeShortCode(),
                iOrderFulfillment.getClassImplement(), iOrderFulfillment.getSource(), iOrderFulfillment.getServiceChannel(),
                iOrderFulfillment.getSendNotificationByChannel());

        CancellationCodeReason codeReason;

        UtilClass utilClass = new UtilClass(iOrderFulfillment.getClassImplement(),iOrderFulfillment.getServiceType(),
                actionDto.getAction(), actionDto.getOrigin(), Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name(),
                iOrderFulfillment.getSendNewFlow());

        Function<List<OrderCanonical>,Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(actionDto, iOrderFulfillment);

        if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())
            || Constant.ActionOrder.REJECT_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

            if (iOrderFulfillment.getSource().equalsIgnoreCase(Constant.Source.SC.name())) {

                OrderExternalService sellerCenterService = (OrderExternalService) context.getBean(
                            Constant.SellerCenter.BEAN_SERVICE_NAME
                );

                log.info("[START] Add controversy because of order cancellation comming from seller center");

                ControversyRequestDto controversyRequestDto = new ControversyRequestDto();
                controversyRequestDto.setDate(DateUtils.getLocalDateTimeNow());
                controversyRequestDto.setText(actionDto.getOrderCancelObservation() != null ? actionDto.getOrderCancelObservation() : "");
                controversyRequestDto.setType(Constant.SellerCenter.ControversyTypes.CT.getType());

                sellerCenterService.addControversy(controversyRequestDto, iOrderFulfillment.getEcommerceId()).subscribe();

                    log.info("[END] add controversy");
            }

            if (actionDto.getOrderCancelCode() != null && actionDto.getOrigin() != null) {
                codeReason = orderCancellationService
                                .geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrigin());
            } else {
                codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
            }

            // Esta condición está para saber si una orden se manda a cancelar pero no está en el tracker porque de
            // seguro falló al momento de registrarse

            if (Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name())
                    || Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())) {

                return externalStoreService
                        .getStoreByCompanyCodeAndLocalCode(iOrderFulfillment.getCompanyCode(), iOrderFulfillment.getCenterCode())
                        .flatMap(resultStore -> adapterTrackerInterface
                                                    .sendOrderTracker(
                                                            ((OrderExternalService)context.getBean(utilClass.getClassToTracker())),
                                                            resultStore,
                                                            iOrderFulfillment.getEcommerceId(),
                                                            iOrderFulfillment.getExternalId(),
                                                            null,
                                                            Constant.OrderStatus.CANCELLED_ORDER.name(),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                                            actionDto.getOrderCancelObservation()
                                                    )
                                                    .flatMap(responses -> getOrderResponse(
                                                            responses,
                                                            iOrderFulfillment.getOrderId(),
                                                            iOrderFulfillment.getEcommerceId(),
                                                            iOrderFulfillment.getExternalId(),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                            actionDto.getOrderCancelObservation(),
                                                            Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                            utilClass.getOnlyTargetComponentTracker(),
                                                            iOrderFulfillment.getSendNewFlow(),
                                                            actionDto.getUpdatedBy(),
                                                            actionDto.getActionDate()
                                                            )
                                                    )
                        )
                        .filter(response -> Constant.OrderStatus.getByName(response.getOrderStatus().getName()).isSuccess())
                        .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(iOrderFulfillment.getEcommerceId(),
                                actionDto.getAction(), null))
                        .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(iOrderFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS)));

            } else {

                return Flux
                        .fromIterable(utilClass.getClassesToSend())
                        .flatMap(objectClass -> ((AdapterInterface)context
                                                        .getBean(objectClass))
                                                        .getResultfromExternalServices(
                                                                ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                                                iOrderFulfillment.getEcommerceId(),
                                                                actionDto,
                                                                iOrderFulfillment.getCompanyCode(),
                                                                iOrderFulfillment.getServiceType(),
                                                                iOrderFulfillment.getOrderId(),
                                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                                                actionDto.getOrderCancelObservation(),
                                                                iOrderFulfillment.getStatusCode(),
                                                                actionDto.getOrigin()

                                                        )
                                                        .flatMap(responses -> getOrderResponse(
                                                                responses,
                                                                iOrderFulfillment.getOrderId(),
                                                                iOrderFulfillment.getEcommerceId(),
                                                                iOrderFulfillment.getExternalId(),
                                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                                actionDto.getOrderCancelObservation(),
                                                                Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                                Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                                                iOrderFulfillment.getSendNewFlow(),
                                                                actionDto.getUpdatedBy(),
                                                                actionDto.getActionDate()
                                                                )
                                                        )
                        )
                        .buffer()
                        .filter(finalResponse ->
                                finalResponse
                                        .stream()
                                        .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                        )
                        .flatMap(resp ->
                                UtilFunctions
                                        .getSuccessResponseFunction
                                        .getMapOrderCanonical(iOrderFulfillment.getEcommerceId(),actionDto.getAction(), null)
                        )
                        .switchIfEmpty(
                                Mono.defer(() -> UtilFunctions
                                                    .getErrorResponseFunction
                                                    .getMapOrderCanonical(iOrderFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS))
                        )
                        .single();
            }

        }

        return Flux
                .fromIterable(utilClass.getClassesToSend())
                .flatMap(objectClass -> ((AdapterInterface)context
                                            .getBean(objectClass))
                                            .getResultfromExternalServices(
                                                    ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                                    iOrderFulfillment.getEcommerceId(),
                                                    actionDto,
                                                    iOrderFulfillment.getCompanyCode(),
                                                    iOrderFulfillment.getServiceType(),
                                                    iOrderFulfillment.getOrderId(),
                                                    null,
                                                    null,
                                                    null,
                                                    iOrderFulfillment.getStatusCode(),
                                                    actionDto.getOrigin()
                                            )
                                            .flatMap(responses -> getOrderResponse(
                                                    responses,
                                                    iOrderFulfillment.getOrderId(),
                                                    iOrderFulfillment.getEcommerceId(),
                                                    iOrderFulfillment.getExternalId(),
                                                    null,
                                                    null,
                                                    Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                    Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                                    iOrderFulfillment.getSendNewFlow(),
                                                    actionDto.getUpdatedBy(),
                                                    actionDto.getActionDate()
                                                    )
                                            )
                )
                .buffer()
                .filter(finalResponse ->
                        finalResponse
                                .stream()
                                .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                )
                .flatMap(publisherNotification)
                .flatMap(resp ->
                        UtilFunctions
                                .getSuccessResponseFunction
                                .getMapOrderCanonical(iOrderFulfillment.getEcommerceId(),actionDto.getAction(), null)
                )
                .switchIfEmpty(Mono.defer(() ->
                        UtilFunctions
                                .getErrorResponseFunction
                                .getMapOrderCanonical(iOrderFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS))
                )
                .single();
    }

    @Override
    public Mono<OrderCanonical> sendOnlyLastStatusOrderFromSync(IOrderFulfillment iOrdersFulfillment, ActionDto actionDto) {


        log.info("sendOnlyLastStatusOrderFromSync, ecommerceId:{}", iOrdersFulfillment.getEcommerceId());

        Function<List<OrderCanonical>,Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(actionDto, iOrdersFulfillment);

        UtilClass utilClass = new UtilClass(iOrdersFulfillment.getClassImplement(),iOrdersFulfillment.getServiceType(),
                actionDto.getAction(), actionDto.getOrigin(),
                Constant.OrderStatus.getByCode(iOrdersFulfillment.getStatusCode()).name(),
                iOrdersFulfillment.getSendNewFlow());

        CancellationCodeReason codeReason;

        if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())
                || Constant.ActionOrder.REJECT_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

            if (actionDto.getOrderCancelCode() != null && actionDto.getOrigin() != null) {
                codeReason = orderCancellationService
                        .geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrigin());
            } else {
                codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
            }

            return Flux
                    .fromIterable(utilClass.getClassesToSend())
                    .flatMap(objectClass -> ((AdapterInterface)context
                            .getBean(objectClass))
                            .getResultfromExternalServices(
                                    ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                    iOrdersFulfillment.getEcommerceId(),
                                    actionDto,
                                    iOrdersFulfillment.getCompanyCode(),
                                    iOrdersFulfillment.getServiceType(),
                                    iOrdersFulfillment.getOrderId(),
                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                    actionDto.getOrderCancelObservation(),
                                    iOrdersFulfillment.getStatusCode(),
                                    actionDto.getOrigin()

                            ).flatMap(responses -> updateTheLastOrderStatusFromHistory(
                                                        responses,
                                                        iOrdersFulfillment.getOrderId(),
                                                        iOrdersFulfillment.getEcommerceId(),
                                                        Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                        actionDto.getOrderCancelObservation(),
                                                        Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                        Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                                        actionDto.getUpdatedBy(),
                                                        actionDto.getActionDate()
                                                    )
                            )
                    )
                    .buffer()
                    .filter(finalResponse ->
                            finalResponse
                                    .stream()
                                    .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                    )
                    .flatMap(resp ->
                            UtilFunctions
                                    .getSuccessResponseFunction
                                    .getMapOrderCanonical(iOrdersFulfillment.getEcommerceId(),actionDto.getAction(), null)
                    )
                    .switchIfEmpty(
                            Mono.defer(() -> UtilFunctions
                                                .getErrorResponseFunction
                                                .getMapOrderCanonical(
                                                        iOrdersFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS)
                            )
                    )
                    .single();
        } else {

            return Flux
                    .fromIterable(utilClass.getClassesToSend())
                    .flatMap(objectClass -> ((AdapterInterface)context
                            .getBean(objectClass))
                            .getResultfromExternalServices(
                                    ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                    iOrdersFulfillment.getEcommerceId(),
                                    actionDto,
                                    iOrdersFulfillment.getCompanyCode(),
                                    iOrdersFulfillment.getServiceType(),
                                    iOrdersFulfillment.getOrderId(),
                                    null,
                                    null,
                                    null,
                                    iOrdersFulfillment.getStatusCode(),
                                    actionDto.getOrigin()
                            )
                            .flatMap(responses -> updateTheLastOrderStatusFromHistory(
                                                    responses,
                                                    iOrdersFulfillment.getOrderId(),
                                                    iOrdersFulfillment.getEcommerceId(),
                                                    null,
                                                    null,
                                                    Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                    Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                                    actionDto.getUpdatedBy(),
                                                    actionDto.getActionDate()
                                                  )
                            )
                    )
                    .buffer()
                    .filter(finalResponse ->
                            finalResponse
                                    .stream()
                                    .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                    )
                    .flatMap(publisherNotification)
                    .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(iOrdersFulfillment.getEcommerceId(),actionDto.getAction(), null))
                    .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(iOrdersFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS)))
                    .single();

        }

    }

    @Override
    public Mono<OrderCanonical> updateOrderStatusListAudit(IOrderFulfillment iOrdersFulfillment, OrderCanonical orderSend,
                                                           HistorySynchronizedDto historySynchronized, String origin) {
        log.info("Sending to audit the list of status, ecommerceId:{}, action:{}",
                iOrdersFulfillment.getEcommerceId(),historySynchronized.getAction());

        OrderCanonical orderCanonical;
        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(historySynchronized.getActionDate());

        Constant.OrderStatus orderStatus =  Constant
                                                .OrderStatusTracker
                                                .getByActionName(historySynchronized.getAction())
                                                .getOrderStatus();

        UtilClass utilClass = new UtilClass(
                                    iOrdersFulfillment.getClassImplement(),iOrdersFulfillment.getServiceType(),
                                    historySynchronized.getAction(), origin, orderStatus.name(),
                                    iOrdersFulfillment.getSendNewFlow());

        if (orderSend.getAction().equalsIgnoreCase(historySynchronized.getAction())) {
            orderCanonical = orderSend;
        } else {

            OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
            orderStatusCanonical.setCode(orderStatus.getCode());
            orderStatusCanonical.setName(orderStatus.name());

            orderCanonical = new OrderCanonical();
            orderCanonical.setOrderStatus(orderStatusCanonical);

        }

        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));
        orderCanonical.getOrderStatus().setCancellationCode(historySynchronized.getOrderCancelCode());
        orderCanonical.getOrderStatus().setCancellationObservation(historySynchronized.getOrderCancelObservation());

        orderCanonical.setEcommerceId(iOrdersFulfillment.getEcommerceId());
        orderCanonical.setSource(origin);

        utilClass
                .getClassesToSend()
                .stream()
                .findFirst()
                .ifPresent(result ->
                        orderCanonical.setTarget(
                                Constant.ClassesImplements
                                        .getByClass(utilClass.getClassImplementationToOrderExternalService(result))
                                        .getTargetName())
                );


        orderCanonical.setAction(historySynchronized.getAction());

        adapterAuditInterface.updateExternalAudit(iOrdersFulfillment.getSendNewFlow(),
                orderCanonical, historySynchronized.getUpdatedBy()
        ).subscribe();

        return Mono.just(orderCanonical);

    }

    @Override
    public Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long id, Long ecommerceId, Long externalId,
                                                 String orderCancelCode, String orderCancelObservation, String source,
                                                 String target, boolean sendNewAudit, String updateBy, String actionDate) {

        log.info("Target to send:{}, updateBy:{}",target,updateBy);
        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation, orderCancelCode,
                orderCanonical.getOrderStatus().getCode(), id, localDateTime,
                Optional.ofNullable(orderCancelCode).map(oc -> localDateTime).orElse(null)
        );

        orderCanonical.setExternalId(externalId);
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        orderCanonical.getOrderStatus().setCancellationCode(orderCancelCode);
        orderCanonical.getOrderStatus().setCancellationObservation(orderCancelObservation);

        if (!orderCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
            adapterAuditInterface.updateExternalAudit(sendNewAudit, orderCanonical, updateBy).subscribe();
        }

        return Mono.just(orderCanonical);

    }

    @Override
    public void createExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical) {

        adapterAuditInterface.createExternalAudit(sendNewAudit, orderAuditCanonical, null).subscribe();
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


    private Mono<OrderCanonical> updateTheLastOrderStatusFromHistory(OrderCanonical orderCanonical, Long id,
                                                                     Long ecommerceId, String orderCancelCode,
                                                                     String orderCancelObservation, String source,
                                                                     String target, String updateBy, String actionDate) {
        log.info("Target to send:{}, updateBy:{}",target,updateBy);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation, orderCancelCode,
                orderCanonical.getOrderStatus().getCode(), id, localDateTime,
                Optional.ofNullable(orderCancelCode).map(oc -> localDateTime).orElse(null)
        );

        return Mono.just(orderCanonical);
    }

    @Override
    public Mono<Boolean> processSendNotification(ActionDto actionDto, IOrderFulfillment iOrderFulfillment) {

        if (iOrderFulfillment.getSendNotificationByChannel()) {

            String statusToSend = Constant
                                    .OrderStatusTracker
                                    .getByActionNameAndServiceTypeCoce(actionDto.getAction(), iOrderFulfillment.getClassImplement());

            String localType = Constant.TrackerImplementation.getIdByClassImplement(iOrderFulfillment.getClassImplement()).getLocalType();

            String expiredDate = null;

            if (Constant.PICKUP.equalsIgnoreCase(iOrderFulfillment.getServiceType())
                    && Constant.ActionOrder.READY_PICKUP_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {


                int daysToExpiredRet = Integer
                                        .parseInt(
                                                applicationParameterService
                                                        .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET)
                                                        .getValue()
                                        );

                expiredDate = DateUtils.getLocalDateWithFormat(iOrderFulfillment.getScheduledTime().plusDays(daysToExpiredRet));

                log.info("Get expired date:{}",expiredDate);
            }

            return adapterNotificationInterface
                        .sendNotification(iOrderFulfillment.getSource(), iOrderFulfillment.getServiceTypeShortCode(),
                                statusToSend, iOrderFulfillment.getEcommerceId(), iOrderFulfillment.getCompanyCode(),
                                iOrderFulfillment.getCenterCode(), localType, iOrderFulfillment.getPhone(),
                                iOrderFulfillment.getFirstName(), expiredDate, null, null
                        );
        }

        return Mono.just(true);

    }

}
