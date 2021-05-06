package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.util.Constant;
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

    public Mono<OrderCanonical> evaluateUpdate(OrderCanonical orderCanonical) {

        return Mono
                .just(getValueBoolenOfParameter())
                .zipWith(Mono.just(getLiquidationStatusByDigitalStatusCode(orderCanonical.getOrderStatus().getCode())),

                        (var1,var2) -> {

                            if (var1 && var2.isLiquidationEnabled()) {
                                // Agregar lógica para enviar al módulo de liquidación,
                                // registro en el DM y audit respectivo
                            }

                            return orderCanonical;
                })
                .switchIfEmpty(Mono.defer(() -> Mono.just(orderCanonical)))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error during evaluate the liquidation process:{}",e.getMessage());

                    return Mono.just(orderCanonical);
                });


    }

    private boolean getValueBoolenOfParameter() {
        return getValueBoolenOfParameter(Constant.ApplicationsParameters.ENABLED_SEND_TO_LIQUIDATION);
    }
}
