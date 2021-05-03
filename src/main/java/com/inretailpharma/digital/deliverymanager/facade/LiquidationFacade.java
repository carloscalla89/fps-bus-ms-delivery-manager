package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.UtilFunctions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LiquidationFacade extends FacadeAbstractUtil {


    public Mono<OrderCanonical> createUpdate(OrderCanonical orderCanonical) {

        if (getValueBoolenOfParameter()) {

            return null;

        } else {
            return Mono.just(orderCanonical);
        }

    }

    public Mono<OrderCanonical> evaluateUpdate(String action, OrderCanonical orderCanonical) {

        if (getValueBoolenOfParameter() && UtilFunctions.processLiquidationStatus.evaluateToSent(action)) {

            return null;

        } else {
            return Mono.just(orderCanonical);
        }

    }




    private boolean getValueBoolenOfParameter() {
        return getValueBoolenOfParameter(Constant.ApplicationsParameters.ENABLED_SEND_TO_LIQUIDATION);
    }
}
