package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.LiquidationAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.LiquidationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LiquidationFacade extends FacadeAbstractUtil {

    private LiquidationAdapter iLiquidationAdapter;

    @Autowired
    public LiquidationFacade(LiquidationAdapter iLiquidationAdapter) {
        this.iLiquidationAdapter = iLiquidationAdapter;
    }

    public Mono<OrderCanonical> createUpdate(OrderCanonical orderCanonical, OrderCanonical completeOrder) {

        if (getValueBoolenOfParameter()) {

            return iLiquidationAdapter.createOrder(completeOrder, orderCanonical);

        } else {
            return Mono.just(orderCanonical);
        }

    }

    public Mono<OrderCanonical> evaluateUpdate(OrderCanonical orderCanonical) {

        if (getValueBoolenOfParameter()) {
            return Mono
                    .just(getLiquidationStatusByDigitalStatusCode(orderCanonical.getOrderStatus().getCode()))
                    .defaultIfEmpty(LiquidationCanonical.builder().enabled(false).build())
                    .filter(LiquidationCanonical::isEnabled)
                    .flatMap(result -> iLiquidationAdapter.updateOrder(orderCanonical, result.getStatus()))
                    .defaultIfEmpty(orderCanonical);
        } else {

            return Mono.just(orderCanonical);
        }

    }

    private boolean getValueBoolenOfParameter() {
        return getValueBoolenOfParameter(Constant.ApplicationsParameters.ENABLED_SEND_TO_LIQUIDATION);
    }
}
