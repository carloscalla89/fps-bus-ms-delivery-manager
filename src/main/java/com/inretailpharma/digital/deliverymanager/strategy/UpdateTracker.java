package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ITrackerAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.HistorySynchronizedDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import com.inretailpharma.digital.deliverymanager.util.UtilClass;
import com.inretailpharma.digital.deliverymanager.util.UtilFunctions;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Component
public class UpdateTracker extends FacadeAbstractUtil implements IActionStrategy {

    private OrderCancellationService orderCancellationService;

    private ApplicationContext context;

    private IAuditAdapter iAuditAdapter;

    @Autowired
    public UpdateTracker(OrderCancellationService orderCancellationService,
                         ApplicationContext context,
                         IAuditAdapter iAuditAdapter) {
        this.orderCancellationService = orderCancellationService;
        this.context = context;
        this.iAuditAdapter = iAuditAdapter;
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

        log.info("sendToUpdateOrder proxy: orderId:{}, ecommerceId:{}, action:{}, serviceType:{}",
                iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), actionDto,
                iOrderFulfillment.getServiceType());


        UtilClass utilClass = new UtilClass(iOrderFulfillment.getClassImplement(),iOrderFulfillment.getServiceType(),
                actionDto.getAction(), actionDto.getOrigin(), Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name());

        Function<List<OrderCanonical>, Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(actionDto, iOrderFulfillment);

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
                                        null,
                                        null,
                                        null,
                                        null

                                )
                                .flatMap(response ->
                                        updateOrderInfulfillment(
                                                response,
                                                iOrderFulfillment.getOrderId(),
                                                iOrderFulfillment.getEcommerceId(),
                                                iOrderFulfillment.getExternalId(),
                                                null,
                                                null,
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
                            null,
                            null,
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

    public Mono<OrderCanonical> sendOnlyLastStatusOrderFromSync(IOrderFulfillment iOrderFulfillment,
                                                                ActionDto actionDto, CancellationCodeReason codeReason) {

        log.info("sendOnlyLastStatusOrderFromSync, ecommerceId:{}", iOrderFulfillment.getEcommerceId());

        Function<List<OrderCanonical>,Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(actionDto, iOrderFulfillment);

        UtilClass utilClass = new UtilClass(iOrderFulfillment.getClassImplement(),iOrderFulfillment.getServiceType(),
                actionDto.getAction(), actionDto.getOrigin(),
                Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name());


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
                                .flatMap(responses -> updateOrderInfulfillment(
                                        responses,
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
                                .getMapOrderCanonical(iOrderFulfillment.getEcommerceId(),actionDto.getAction(), null,
                                        utilClass.getFirstOrderStatusName(), iOrderFulfillment.getOrderId(), utilClass.getServiceType(),
                                        actionDto.getOrderCancelCode()))
                .switchIfEmpty(Mono.defer(() ->
                        UtilFunctions.getErrorResponseFunction.getMapOrderCanonical(
                                iOrderFulfillment.getEcommerceId(), actionDto.getAction(), Constant.ERROR_PROCESS,
                                null, iOrderFulfillment.getOrderId(), utilClass.getServiceType(),
                                actionDto.getOrderCancelCode()))
                )
                .single();
    }

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
                historySynchronized.getAction(), origin, orderStatus.name());

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

}