package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.adapter.AdapterInterface;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.GenericValidator;
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
    public Mono<OrderCanonical> sendOrderToTracker(Long orderId, Long ecommerceId, Long externalId, String classImplementTracker,
                                                   String statusDetail, String statusName, String orderCancelCode,
                                                   String orderCancelDescription, String orderCancelObservation,
                                                   String companyCode, String localCode, String source,
                                                   boolean sendNewAudit, String updateBy,com.inretailpharma.digital.deliverymanager.dto.OrderDto orderDto) {

        log.info("Send to create the order in tracker when its sent by reattempt");

        UtilClass utilClass = new UtilClass(classImplementTracker);


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
                                                        orderCancelDescription,
                                                        orderCancelObservation,
                                                        orderDto
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
                                                )
                    );

    }

    @Override
    public Mono<OrderCanonical> sendToUpdateOrder(Long orderId, Long ecommerceId, Long externalId, ActionDto actionDto,
                                                  String serviceType, String serviceShortCode, String classImplementTracker,
                                                  String source, String channel, String companyCode, String localCode,
                                                  String statusCode, String clientName, String phone, LocalDateTime scheduledTime,
                                                  boolean sendNewFlow, boolean sendNotificationByChannel) {

        log.info("sendToUpdateOrder proxy: orderId:{}, ecommerceId:{}, action:{}, sendNewFlow:{}, serviceType:{}, " +
                 "serviceShortCode:{}, classImplementTracker:{}, source:{}, channel:{}, sendNotificationByChannel:{}," +
                 "statusCode:{}", orderId, ecommerceId, actionDto, sendNewFlow, serviceType, serviceShortCode,
                classImplementTracker, source,channel, sendNotificationByChannel, statusCode);

        CancellationCodeReason codeReason;

        UtilClass utilClass = new UtilClass(classImplementTracker,serviceType, actionDto.getAction(), actionDto.getOrigin(),
                Constant.OrderStatus.getByCode(statusCode).name(), sendNewFlow);

        Function<List<OrderCanonical>,Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(ecommerceId, actionDto, serviceShortCode,
                        classImplementTracker, source, companyCode, localCode, clientName, phone, serviceType,
                        sendNotificationByChannel, scheduledTime);

        if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())
            || Constant.ActionOrder.REJECT_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

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

            if (actionDto.getOrderCancelCode() != null && actionDto.getOrigin() != null) {
                codeReason = orderCancellationService
                                .geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrigin());
            } else {
                codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
            }

            // Esta condición está para saber si una orden se manda a cancelar pero no está en el tracker porque de
            // seguro falló al momento de registrarse

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
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                                            actionDto.getOrderCancelObservation(),null
                                                    )
                                                    .flatMap(responses -> getOrderResponse(
                                                            responses,
                                                            orderId,
                                                            ecommerceId,
                                                            externalId,
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                            actionDto.getOrderCancelObservation(),
                                                            Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                            utilClass.getOnlyTargetComponentTracker(),
                                                            sendNewFlow,
                                                            actionDto.getUpdatedBy(),
                                                            actionDto.getActionDate()
                                                            )
                                                    )
                        )
                        .filter(response -> Constant.OrderStatus.getByName(response.getOrderStatus().getName()).isSuccess())
                        //.flatMap(publisherNotificationSingle)
                        .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(ecommerceId,actionDto.getAction(), null))
                        .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(ecommerceId, actionDto.getAction(), Constant.ERROR_PROCESS)));

            } else {

                return Flux
                        .fromIterable(utilClass.getClassesToSend())
                        .flatMap(objectClass -> ((AdapterInterface)context
                                                        .getBean(objectClass))
                                                        .getResultfromExternalServices(
                                                                ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                                                ecommerceId,
                                                                actionDto,
                                                                companyCode,
                                                                serviceType,
                                                                orderId,
                                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                                                actionDto.getOrderCancelObservation(),
                                                                statusCode,
                                                                actionDto.getOrigin()

                                                        )
                                                        .flatMap(responses -> getOrderResponse(
                                                                responses,
                                                                orderId,
                                                                ecommerceId,
                                                                externalId,
                                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                                actionDto.getOrderCancelObservation(),
                                                                Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                                Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                                                sendNewFlow,
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
                        .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(ecommerceId,actionDto.getAction(), null))
                        .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(ecommerceId, actionDto.getAction(), Constant.ERROR_PROCESS)))
                        .single();
            }

        }

        return Flux
                .fromIterable(utilClass.getClassesToSend())
                .flatMap(objectClass -> ((AdapterInterface)context
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
                                                    null,
                                                    statusCode,
                                                    actionDto.getOrigin()
                                            )
                                            .flatMap(responses -> getOrderResponse(
                                                    responses,
                                                    orderId,
                                                    ecommerceId,
                                                    externalId,
                                                    null,
                                                    null,
                                                    Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                    Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                                    sendNewFlow,
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
                .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(ecommerceId,actionDto.getAction(), null))
                .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(ecommerceId, actionDto.getAction(), Constant.ERROR_PROCESS)))
                .single();
    }

    @Override
    public Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long id, Long ecommerceId, Long externalId,
                                                 String orderCancelCode, String orderCancelObservation, String source,
                                                 String target, boolean sendNewAudit, String updateBy, String actionDate) {
        log.info("Target to send:{}, updateBy:{}",target,updateBy);
        LocalDateTime localDateTime = Optional
                                        .ofNullable(actionDate)
                                        .filter(DateUtils::validFormatDateTimeFormat)
                                        .map(DateUtils::getLocalDateTimeFromStringWithFormat)
                                        .orElseGet(DateUtils::getLocalDateTimeObjectNow);

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

    private Mono<Boolean> processSendNotification(Long ecommerceId, ActionDto actionDto, String serviceShortCode,
                                                  String classImplementTracker, String source, String companyCode,
                                                  String localCode, String clientName, String phone, String serviceType,
                                                  boolean sendNotificationByChannel, LocalDateTime scheduledDate) {

        if (sendNotificationByChannel) {

            String statusToSend = Constant
                                    .OrderStatusTracker
                                    .getByActionNameAndServiceTypeCoce(actionDto.getAction(), classImplementTracker);

            String localType = Constant.TrackerImplementation.getIdByClassImplement(classImplementTracker).getLocalType();

            String expiredDate = null;

            if (Constant.PICKUP.equalsIgnoreCase(serviceType)
                    && Constant.ActionOrder.READY_PICKUP_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {


                int daysToExpiredRet = Integer
                                        .parseInt(
                                                applicationParameterService
                                                        .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET)
                                                        .getValue()
                                        );

                expiredDate = DateUtils.getLocalDateWithFormat(scheduledDate.plusDays(daysToExpiredRet));

                log.info("Get expired date:{}",expiredDate);
            }

            return adapterNotificationInterface
                        .sendNotification(source, serviceShortCode, statusToSend, ecommerceId, companyCode, localCode,
                                localType, phone, clientName, expiredDate, null, null
                        );
        }

        return Mono.just(true);

    }

}
