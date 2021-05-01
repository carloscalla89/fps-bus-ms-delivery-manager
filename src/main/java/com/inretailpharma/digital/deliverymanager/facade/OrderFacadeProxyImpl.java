package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.adapter.*;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.HistorySynchronizedDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
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
public class OrderFacadeProxyImpl extends FacadeAbstractUtil implements OrderFacadeProxy{

    private OrderExternalService externalOnlinePaymentService;

    private ITrackerAdapter iTrackerAdapter;

    private IAuditAdapter iAuditAdapter;

    private IStoreAdapter iStoreAdapter;

    private final ApplicationContext context;

    @Autowired
    public OrderFacadeProxyImpl(@Qualifier("onlinePayment") OrderExternalService externalOnlinePaymentService,
                                @Qualifier("trackerAdapter") ITrackerAdapter iTrackerAdapter,
                                @Qualifier("auditAdapter") IAuditAdapter iAuditAdapter,
                                @Qualifier("storeAdapter") IStoreAdapter iStoreAdapter,
                                ApplicationContext context) {

        this.externalOnlinePaymentService = externalOnlinePaymentService;
        this.iTrackerAdapter = iTrackerAdapter;
        this.iAuditAdapter = iAuditAdapter;
        this.iStoreAdapter = iStoreAdapter;
        this.context = context;
    }

    @Override
    public Mono<OrderCanonical> sendToUpdateOrder(IOrderFulfillment iOrderFulfillment, ActionDto actionDto,
                                                  CancellationCodeReason codeReason) {

        log.info("sendToUpdateOrder proxy: orderId:{}, ecommerceId:{}, action:{}, sendNewFlow:{}, serviceType:{}, " +
                 "serviceShortCode:{}, classImplementTracker:{}, source:{}, channel:{}, sendNotificationByChannel:{}",
                iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), actionDto, iOrderFulfillment.getSendNewFlow(),
                iOrderFulfillment.getServiceType(), iOrderFulfillment.getServiceTypeShortCode(),
                iOrderFulfillment.getClassImplement(), iOrderFulfillment.getSource(), iOrderFulfillment.getServiceChannel(),
                iOrderFulfillment.getSendNotificationByChannel());

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

            // Esta condición está para saber si una orden se manda a cancelar pero no está en el tracker porque de
            // seguro falló al momento de registrarse

            if (Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name())
                    || Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())) {

                return iStoreAdapter
                        .getStoreByCompanyCodeAndLocalCode(iOrderFulfillment.getCompanyCode(), iOrderFulfillment.getCenterCode())
                        .flatMap(resultStore -> iTrackerAdapter
                                                    .evaluateTracker(
                                                            Constant.TrackerImplementation
                                                                    .getClassImplement(iOrderFulfillment.getClassImplement())
                                                                    .getTrackerImplement(),
                                                            actionDto,
                                                            resultStore,
                                                            iOrderFulfillment.getCompanyCode(),
                                                            iOrderFulfillment.getServiceType(),
                                                            iOrderFulfillment.getEcommerceId(),
                                                            iOrderFulfillment.getExternalId(),
                                                            Constant.OrderStatus.CANCELLED_ORDER.name(),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                                            actionDto.getOrderCancelObservation(),
                                                            null
                                                    )
                                                    .flatMap(response ->
                                                            updateOrderInfulfillment(
                                                                    response,
                                                                    iOrderFulfillment.getOrderId(),
                                                                    iOrderFulfillment.getEcommerceId(),
                                                                    iOrderFulfillment.getExternalId(),
                                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                                    actionDto.getOrderCancelObservation(),
                                                                    Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                                    utilClass.getOnlyTargetComponentTracker(),
                                                                    actionDto.getUpdatedBy(),
                                                                    actionDto.getActionDate()
                                                            )
                                                    )
                                                    .flatMap(response -> iAuditAdapter.updateAudit(response, actionDto.getUpdatedBy()))

                        )
                        .filter(response -> Constant.OrderStatus.getByName(response.getOrderStatus().getName()).isSuccess())
                        .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(iOrderFulfillment.getEcommerceId(),
                                actionDto.getAction(), null))
                        .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(iOrderFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS)));

            }

        }

        return Flux
                .fromIterable(utilClass.getClassesToSend())
                .flatMap(objectClass ->
                        ((ITrackerAdapter)context
                                .getBean(objectClass))
                                .evaluateTracker(
                                        utilClass.getClassImplementationToOrderExternalService(objectClass),
                                        actionDto,
                                        null,
                                        iOrderFulfillment.getCompanyCode(),
                                        iOrderFulfillment.getServiceType(),
                                        iOrderFulfillment.getEcommerceId(),
                                        iOrderFulfillment.getExternalId(),
                                        iOrderFulfillment.getStatusName(),
                                        Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                        Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                        actionDto.getOrderCancelObservation(),
                                        null

                                )
                                .flatMap(response ->
                                    updateOrderInfulfillment(
                                            response,
                                            iOrderFulfillment.getOrderId(),
                                            iOrderFulfillment.getEcommerceId(),
                                            iOrderFulfillment.getExternalId(),
                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                            actionDto.getOrderCancelObservation(),
                                            Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                            Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                            actionDto.getUpdatedBy(),
                                            actionDto.getActionDate()
                                    )
                                )
                                .flatMap(response -> iAuditAdapter.updateAudit(response, actionDto.getUpdatedBy()))

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
    public Mono<OrderCanonical> sendOnlyLastStatusOrderFromSync(IOrderFulfillment iOrderFulfillment,
                                                                ActionDto actionDto, CancellationCodeReason codeReason) {

        log.info("sendOnlyLastStatusOrderFromSync, ecommerceId:{}", iOrderFulfillment.getEcommerceId());

        Function<List<OrderCanonical>,Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(actionDto, iOrderFulfillment);

        UtilClass utilClass = new UtilClass(iOrderFulfillment.getClassImplement(),iOrderFulfillment.getServiceType(),
                actionDto.getAction(), actionDto.getOrigin(),
                Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name(),
                iOrderFulfillment.getSendNewFlow());


        return Flux
                .fromIterable(utilClass.getClassesToSend())
                .flatMap(objectClass ->
                        ((ITrackerAdapter)context
                                .getBean(objectClass))
                                .evaluateTracker(
                                        utilClass.getClassImplementationToOrderExternalService(objectClass),
                                        actionDto,
                                        null,
                                        iOrderFulfillment.getCompanyCode(),
                                        iOrderFulfillment.getServiceType(),
                                        iOrderFulfillment.getEcommerceId(),
                                        iOrderFulfillment.getExternalId(),
                                        iOrderFulfillment.getStatusName(),
                                        Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                        Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                        actionDto.getOrderCancelObservation(),
                                        null
                                )
                                .flatMap(responses -> updateOrderInfulfillment(
                                        responses,
                                        iOrderFulfillment.getOrderId(),
                                        iOrderFulfillment.getEcommerceId(),
                                        iOrderFulfillment.getExternalId(),
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
                .flatMap(publisherNotification)
                .flatMap(resp -> UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(iOrderFulfillment.getEcommerceId(),actionDto.getAction(), null))
                .switchIfEmpty(Mono.defer(() -> UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(iOrderFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS)))
                .single();
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

        return iAuditAdapter.updateAudit(orderCanonical, historySynchronized.getUpdatedBy()).flatMap(Mono::just);

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
                        updateOrderOnlinePaymentStatusByExternalId(orderId,onlinePaymentStatus);
                    }
                    log.info("[END] to update order");
                    return r;
                });

    }
}
