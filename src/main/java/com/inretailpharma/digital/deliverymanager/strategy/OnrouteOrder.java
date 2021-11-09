package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ISellerCenterAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ITrackerAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.UtilClass;
import com.inretailpharma.digital.deliverymanager.util.UtilFunctions;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Component
public class OnrouteOrder extends FacadeAbstractUtil implements IActionStrategy{

    private ApplicationContext context;
    private IAuditAdapter iAuditAdapter;
    private ISellerCenterAdapter iSellerCenterAdapter;

    public OnrouteOrder(ApplicationContext context, IAuditAdapter iAuditAdapter, ISellerCenterAdapter iSellerCenterAdapter) {
        this.context = context;
        this.iAuditAdapter = iAuditAdapter;
        this.iSellerCenterAdapter = iSellerCenterAdapter;
    }

    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {
        return  Optional
                .ofNullable(getOnlyOrderByecommerceId(ecommerceId))
                .filter(val -> !Constant.OrderStatus.getFinalStatusByCode(val.getStatusCode()))
                .isPresent();
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {
        IOrderFulfillment iOrderFulfillment = getOrderLightByecommerceId(ecommerceId);

        log.info("prepareOrder update proxy: orderId:{}, ecommerceId:{}, action:{},",
                iOrderFulfillment.getOrderId(), iOrderFulfillment.getEcommerceId(), actionDto);

        UtilClass utilClass = new UtilClass(iOrderFulfillment.getClassImplement(),iOrderFulfillment.getServiceType(),
                actionDto.getAction(), actionDto.getOrigin(), Constant.OrderStatus.getByCode(iOrderFulfillment.getStatusCode()).name());

        Function<List<OrderCanonical>, Publisher<? extends Boolean>> publisherNotification =
                responses -> processSendNotification(actionDto, iOrderFulfillment);


        if (iOrderFulfillment.getSource().equalsIgnoreCase(Constant.SOURCE_SELLER_CENTER)) {
            iSellerCenterAdapter
                    .updateStatusOrderSeller(ecommerceId, actionDto.getAction())
                    .doOnNext(val -> log.info("result updateStatusOrderSeller seller onrouter:{}",val))
                    .flatMap(orderCanonical -> getDataToSentAudit(orderCanonical, actionDto))
                    .doOnNext(val -> log.info("result getDataToSentAudit seller onrouter:{}",val))
                    .map(orderCanonical -> iAuditAdapter.updateAudit(orderCanonical, actionDto.getUpdatedBy()))
                    .subscribe();
        }

        // validar si el source es de seller center para llamar al componente

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
                                        actionDto.getOrderCancelObservation(),
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
}
