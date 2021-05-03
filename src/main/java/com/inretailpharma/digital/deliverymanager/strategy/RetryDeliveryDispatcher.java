package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.*;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class RetryDeliveryDispatcher extends FacadeAbstractUtil implements IActionStrategy{

    @Autowired
    private IStoreAdapter iStoreAdapter;

    @Autowired
    @Qualifier("trackerAdapter")
    private ITrackerAdapter iTrackerAdapter;

    @Autowired
    private IDeliveryDispatcherAdapter iDeliveryDispatcherAdapter;

    @Autowired
    private IAuditAdapter iAuditAdapter;

    public RetryDeliveryDispatcher () {

    }

    @Autowired
    public RetryDeliveryDispatcher(IStoreAdapter iStoreAdapter, @Qualifier("trackerAdapter") ITrackerAdapter iTrackerAdapter,
                                   IDeliveryDispatcherAdapter iDeliveryDispatcherAdapter, IAuditAdapter iAuditAdapter) {
        this.iStoreAdapter = iStoreAdapter;
        this.iTrackerAdapter = iTrackerAdapter;
        this.iDeliveryDispatcherAdapter = iDeliveryDispatcherAdapter;
        this.iAuditAdapter = iAuditAdapter;
    }

    @Override
    public boolean getAction(String action) {
        return Constant.ActionOrder.ATTEMPT_INSINK_CREATE.name().equalsIgnoreCase(action);
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {

        IOrderFulfillment iOrderFulfillmentLight = getOrderLightByecommerceId(ecommerceId);

        return  iStoreAdapter.getStoreByCompanyCodeAndLocalCode(
                iOrderFulfillmentLight.getCompanyCode(), iOrderFulfillmentLight.getCenterCode()
                )
                .flatMap(store -> iDeliveryDispatcherAdapter
                        .sendRetryInsink(
                                iOrderFulfillmentLight.getEcommerceId(),
                                iOrderFulfillmentLight.getCompanyCode(),
                                store
                        )
                        .flatMap(orderResp -> {
                            log.info("Response status:{}, ecommerceId:{}, externalId:{} from dispatcher",
                                    orderResp.getOrderStatus(), orderResp.getEcommerceId(), orderResp.getExternalId());
                            if ((Constant
                                    .OrderStatus
                                    .getByCode(Optional
                                            .ofNullable(orderResp.getOrderStatus())
                                            .map(OrderStatusCanonical::getCode)
                                            .orElse(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                                    ).isSuccess())) {

                                iTrackerAdapter
                                        .evaluateTracker(
                                                Constant.TrackerImplementation
                                                        .getClassImplement(iOrderFulfillmentLight.getClassImplement())
                                                        .getTrackerImplement(),
                                                actionDto,
                                                store,
                                                iOrderFulfillmentLight.getCompanyCode(),
                                                iOrderFulfillmentLight.getServiceType(),
                                                iOrderFulfillmentLight.getEcommerceId(),
                                                orderResp.getExternalId(),

                                                Optional.ofNullable(orderResp.getOrderStatus())
                                                        .filter(r -> r.getName().equalsIgnoreCase(Constant.OrderStatusTracker.CONFIRMED.name()))
                                                        .map(r -> Constant.OrderStatusTracker.CONFIRMED_TRACKER.name())
                                                        .orElse(Optional.ofNullable(orderResp.getOrderStatus()).map(OrderStatusCanonical::getName).orElse(null)),

                                                Optional.ofNullable(orderResp.getOrderStatus())
                                                        .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                        .orElse(null),

                                                Optional.ofNullable(orderResp.getOrderStatus())
                                                        .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getReason())
                                                        .orElse(null),
                                                null,
                                                Optional.ofNullable(orderResp.getOrderStatus())
                                                        .filter(d -> !StringUtils.isEmpty(d.getDetail()))
                                                        .map(OrderStatusCanonical::getDetail)
                                                        .orElse(null)
                                        )
                                        .flatMap(response ->
                                                updateOrderInfulfillment(
                                                        response,
                                                        iOrderFulfillmentLight.getOrderId(),
                                                        iOrderFulfillmentLight.getEcommerceId(),
                                                        orderResp.getExternalId(),
                                                        Optional.ofNullable(orderResp.getOrderStatus())
                                                                .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                                .orElse(null),
                                                        null,
                                                        iOrderFulfillmentLight.getSource(),
                                                        Constant.TARGET_INSINK,
                                                        actionDto.getUpdatedBy(),
                                                        null

                                                )
                                        );

                            }

                            return updateOrderInfulfillment(
                                    orderResp,
                                    iOrderFulfillmentLight.getOrderId(),
                                    iOrderFulfillmentLight.getEcommerceId(),
                                    orderResp.getExternalId(),
                                    null,
                                    null,
                                    iOrderFulfillmentLight.getSource(),
                                    Constant.TARGET_INSINK,
                                    actionDto.getUpdatedBy(),
                                    null
                            );


                        }).flatMap(response ->
                                iAuditAdapter.updateAudit(response, actionDto.getUpdatedBy())
                        )



                );
    }
}
