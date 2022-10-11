package com.inretailpharma.digital.deliverymanager.strategy;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.inretailpharma.digital.deliverymanager.adapter.*;
import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import com.inretailpharma.digital.deliverymanager.util.UtilClass;
import com.inretailpharma.digital.deliverymanager.util.UtilFunctions;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class CancelOrder extends FacadeAbstractUtil implements IActionStrategy{

	private OrderCancellationService orderCancellationService;

    private IStoreAdapter iStoreAdapter;

    private ApplicationContext context;

    private ITrackerAdapter iTrackerAdapter;

    private IAuditAdapter iAuditAdapter;

    private PaymentAdapter paymentAdapter;
    
    private OrderExternalService stockService;

    private ISellerCenterAdapter iSellerCenterAdapter;
    
    @Value("${controversy.type}")
    private String controversyType;
    
    @Autowired
    public CancelOrder(OrderCancellationService orderCancellationService, IStoreAdapter iStoreAdapter,
                       ApplicationContext context, @Qualifier("trackerAdapter")ITrackerAdapter iTrackerAdapter,
                       IAuditAdapter iAuditAdapter, PaymentAdapter paymentAdapter,
                       @Qualifier("stock")OrderExternalService stockService,
                       ISellerCenterAdapter iSellerCenterAdapter) {
    	
        this.orderCancellationService = orderCancellationService;
        this.iStoreAdapter = iStoreAdapter;
        this.context = context;
        this.iTrackerAdapter = iTrackerAdapter;
        this.iAuditAdapter = iAuditAdapter;
        this.paymentAdapter = paymentAdapter;
        this.stockService = stockService;
        this.iSellerCenterAdapter = iSellerCenterAdapter;
    }
    
    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {

       return  Optional
                .ofNullable(getOnlyOrderByecommerceId(ecommerceId))
                .filter(val -> {

                    if (Constant.OrderStatus.getFinalStatusByCode(val.getStatusCode())) {

                        return (actionDto.getOrderCancelCode() != null &&
                                actionDto.getOrderCancelCode().equalsIgnoreCase(Constant.ORIGIN_BBR)) ||
                                (actionDto.getOrderCancelObservation() != null && actionDto.getOrderCancelObservation().contains(Constant.ORIGIN_BBR));
                    }

                    return true;

                })
                .isPresent();
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {

        IOrderFulfillment iOrderFulfillment = getOrderLightByecommerceId(ecommerceId);

        log.info("sendToUpdateOrder proxy: orderId:{}, ecommerceId:{}, action:{}, serviceType:{}, serviceShortCode:{}, " +
                        "classImplementTracker:{}, source:{}, channel:{}, sendNotificationByChannel:{}",
                iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), actionDto,
                iOrderFulfillment.getServiceType(), iOrderFulfillment.getServiceTypeShortCode(),
                iOrderFulfillment.getClassImplement(), iOrderFulfillment.getSource(), iOrderFulfillment.getServiceChannel(),
                iOrderFulfillment.getSendNotificationByChannel());

        if ((actionDto.getOrderCancelCode() != null &&
                actionDto.getOrderCancelCode().equalsIgnoreCase(Constant.ORIGIN_BBR)) ||
                (actionDto.getOrderCancelObservation() != null && actionDto.getOrderCancelObservation().contains(Constant.ORIGIN_BBR))) {

            return paymentAdapter.getfromOnlinePayment(iOrderFulfillment, actionDto);

        }

        if (iOrderFulfillment.getSource().equalsIgnoreCase(Constant.SOURCE_SELLER_CENTER)) {
            iSellerCenterAdapter
                    .updateStatusOrderSeller(ecommerceId, actionDto.getAction())
                    .flatMap(orderCanonical -> getDataToSentAudit(orderCanonical, actionDto))
                    .map(orderCanonical -> iAuditAdapter.updateAudit(orderCanonical, actionDto.getUpdatedBy()))
                    .subscribe();
        }

        CancellationCodeReason codeReason = orderCancellationService.evaluateGetCancel(actionDto);

        log.info("cancellationCodeReason:{}",codeReason);

        UtilClass utilClass = new UtilClass(iOrderFulfillment.getClassImplement(),iOrderFulfillment.getServiceType(),
                actionDto.getAction(), actionDto.getOrigin(), Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name(),
                iOrderFulfillment.getExternalRouting());

        Function<List<OrderCanonical>, Publisher<? extends Boolean>> publisherNotification =
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
                controversyRequestDto.setControversyStatus(Constant.SellerCenter.ControversyTypes.CP.getType());
                controversyRequestDto.setMonto(null); //Opcional, si es un reembolso pueden colocar aqui el monto como informativo
                controversyRequestDto.setType(controversyType);
                sellerCenterService.addControversy(controversyRequestDto, iOrderFulfillment.getEcommerceId()).subscribe();

                log.info("[END] add controversy");
            }

            // Esta condición está para saber si una orden se manda a cancelar pero no está en el tracker porque de
            // seguro falló al momento de registrarse

            if (Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name())
                    || Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())) {

                utilClass.setFirstOrderStatusName(Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name());

                return iStoreAdapter
                        .getStoreByCompanyCodeAndLocalCode(iOrderFulfillment.getCompanyCode(), iOrderFulfillment.getCenterCode())
                        .flatMap(resultStore -> iTrackerAdapter
                                                    .createOrderToTracker(
                                                            Constant.TrackerImplementation
                                                                    .getClassImplement(iOrderFulfillment.getClassImplement())
                                                                    .getTrackerImplement(),
                                                            resultStore,
                                                            iOrderFulfillment.getEcommerceId(),
                                                            iOrderFulfillment.getExternalId(),
                                                            Constant.OrderStatus.CANCELLED_ORDER.name(),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(actionDto.getOrderCancelCode()),
                                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getReason).orElse(null),
                                                            actionDto.getOrderCancelObservation(),
                                                            iOrderFulfillment.getStatusDetail(),
                                                            actionDto
                                                    )
                                                    .flatMap(response ->
                                                            updateOrderInfulfillment(
                                                                    response,
                                                                    iOrderFulfillment.getOrderId(),
                                                                    iOrderFulfillment.getEcommerceId(),
                                                                    iOrderFulfillment.getExternalId(),
                                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(actionDto.getOrderCancelCode()),
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
                        .flatMap(resp ->
                                UtilFunctions.getSuccessResponseFunction.getMapOrderCanonical(
                                        iOrderFulfillment.getEcommerceId(), Constant.ActionOrder.CANCEL_ORDER.name(), null,
                                        utilClass.getFirstOrderStatusName(), iOrderFulfillment.getOrderId(), utilClass.getServiceType(),
                                        actionDto.getOrderCancelCode()
                                )
                        )
                        .switchIfEmpty(Mono.defer(() ->
                                        UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(
                                                iOrderFulfillment.getEcommerceId(), Constant.ActionOrder.CANCEL_ORDER.name(),
                                                Constant.ERROR_PROCESS, null, iOrderFulfillment.getOrderId(), utilClass.getServiceType(),
                                                actionDto.getOrderCancelCode()
                                        )
                                )
                        );

            }

        }

        var cancelCode = StringUtils.upperCase(StringUtils.trim(actionDto.getOrderCancelCode()));
        var requiresRelease = !iOrderFulfillment.getVoucher() &&
                (actionDto.getOrigin() != null ||
                        (actionDto.getOrigin() == null && Constant.DU_CANCEL_CODE.equals(cancelCode)));

        log.info("[RELEASE STOCK] order {} - origin {} - cancelCode {} - voucher {} - requiresRelease {}"
                , ecommerceId, actionDto.getOrigin(), cancelCode, iOrderFulfillment.getVoucher(), requiresRelease);

        if (requiresRelease) { stockService.releaseStock(ecommerceId).subscribe(); }

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
                                        Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(actionDto.getOrderCancelCode()),
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
                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(actionDto.getOrderCancelCode()),
                                                actionDto.getOrderCancelObservation(),
                                                Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                                Constant.ClassesImplements.getByClass(utilClass.getClassImplementationToOrderExternalService(objectClass)).getTargetName(),
                                                actionDto.getUpdatedBy(),
                                                actionDto.getActionDate()
                                        )
                                )
                                .flatMap(response -> iAuditAdapter.updateAudit(response, actionDto.getUpdatedBy()))
                )
                .switchIfEmpty(Flux.defer(() -> {

                    Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(actionDto.getAction()).getOrderStatus();

                    OrderStatusCanonical status = new OrderStatusCanonical();
                    status.setCode(orderStatus.getCode());
                    status.setName(orderStatus.name());
                    status.setSuccessful(true);
                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setOrderStatus(status);

                    return updateOrderInfulfillment(
                            orderCanonical,
                            iOrderFulfillment.getOrderId(),
                            iOrderFulfillment.getEcommerceId(),
                            iOrderFulfillment.getExternalId(),
                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(actionDto.getOrderCancelCode()),
                            actionDto.getOrderCancelObservation(),
                            Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                            Constant.ClassesImplements.getByClass(utilClass.getClassToTracker()).getTargetName(),
                            actionDto.getUpdatedBy(),
                            actionDto.getActionDate()
                    ).flatMap(response -> iAuditAdapter.updateAudit(response, actionDto.getUpdatedBy()));
                }))
                .buffer()
                .filter(finalResponse ->
                        finalResponse
                                .stream()
                                .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                )
                .flatMap(publisherNotification)
                .flatMap(r -> this.notifyRouting(ecommerceId,
                		iOrderFulfillment.getExternalRouting(),
                		iOrderFulfillment.getServiceType(),
                		actionDto.getAction(),
                		actionDto.getOrigin()))
                .flatMap(resp ->
                        UtilFunctions
                                .getSuccessResponseFunction
                                .getMapOrderCanonical(
                                        iOrderFulfillment.getEcommerceId(),actionDto.getAction(), null,
                                        utilClass.getFirstOrderStatusName(), iOrderFulfillment.getOrderId(), utilClass.getServiceType(),
                                        actionDto.getOrderCancelCode()
                                )
                )
                .switchIfEmpty(Mono.defer(() ->
                        UtilFunctions
                                .getErrorResponseFunction
                                .getMapOrderCanonical(iOrderFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS,
                                        null, iOrderFulfillment.getOrderId(), utilClass.getServiceType(),
                                        actionDto.getOrderCancelCode()))
                )
                .single();

    }

}
