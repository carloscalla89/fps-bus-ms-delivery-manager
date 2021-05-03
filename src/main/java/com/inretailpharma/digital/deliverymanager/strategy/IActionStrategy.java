package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import reactor.core.publisher.Mono;

public interface IActionStrategy {

    boolean getAction(String action);
    Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId);

    default Mono<OrderCanonical> evaluate(ActionDto actionDto, String ecommerceId) {

        if (getAction(actionDto.getAction()) ) {

            throw new IllegalArgumentException("Wrong action type " + actionDto + "or not exist");
        }
        return process(actionDto, Long.parseLong(ecommerceId));
    }

}
