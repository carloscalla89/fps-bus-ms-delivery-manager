package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ILiquidationAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.IStoreAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ITrackerAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class RetryTracker extends FacadeAbstractUtil implements IActionStrategy {

    @Autowired
    private IStoreAdapter iStoreAdapter;

    @Autowired
    @Qualifier("trackerAdapter")
    private ITrackerAdapter iTrackerAdapter;

    @Autowired
    private IAuditAdapter iAuditAdapter;

    public RetryTracker() {

    }

    @Autowired
    public RetryTracker(IStoreAdapter iStoreAdapter, @Qualifier("trackerAdapter") ITrackerAdapter iTrackerAdapter) {
        this.iStoreAdapter = iStoreAdapter;
        this.iTrackerAdapter = iTrackerAdapter;
    }

    @Override
    public boolean getAction(String action) {
        return Constant.ActionOrder.ATTEMPT_TRACKER_CREATE.name().equalsIgnoreCase(action);
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {

        IOrderFulfillment iOrderFulfillmentLight = getOrderLightByecommerceId(ecommerceId);

        return  iStoreAdapter.getStoreByCompanyCodeAndLocalCode(
                iOrderFulfillmentLight.getCompanyCode(), iOrderFulfillmentLight.getCenterCode()
        ).flatMap(store -> iTrackerAdapter
                            .evaluateTracker(
                                    Constant.TrackerImplementation
                                            .getClassImplement(iOrderFulfillmentLight.getClassImplement())
                                            .getTrackerImplement(),
                                    actionDto,
                                    store,
                                    iOrderFulfillmentLight.getCompanyCode(),
                                    iOrderFulfillmentLight.getServiceType(),
                                    iOrderFulfillmentLight.getEcommerceId(),
                                    iOrderFulfillmentLight.getExternalId(),
                                    Constant.OrderStatus.CONFIRMED_TRACKER.name(),
                                    null,
                                    null,
                                    null,
                                    null
                            )
                            .flatMap(response ->
                                    updateOrderInfulfillment(
                                            response,
                                            iOrderFulfillmentLight.getOrderId(),
                                            iOrderFulfillmentLight.getEcommerceId(),
                                            iOrderFulfillmentLight.getExternalId(),
                                            null,
                                            actionDto.getOrderCancelObservation(),
                                            Optional.ofNullable(actionDto.getOrigin()).orElse(Constant.ORIGIN_UNIFIED_POS),
                                            Constant.TrackerImplementation.getClassImplement(iOrderFulfillmentLight.getClassImplement()).getTargetName(),
                                            actionDto.getUpdatedBy(),
                                            actionDto.getActionDate()
                                    )
                            )
                            .flatMap(response -> iAuditAdapter.updateAudit(response, actionDto.getUpdatedBy()))
        );
    }
}
