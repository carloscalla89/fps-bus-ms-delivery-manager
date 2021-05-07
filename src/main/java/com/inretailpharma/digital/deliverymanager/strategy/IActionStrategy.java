package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IActionStrategy {

    boolean getAction(String action);

    boolean validationStatusOrder(Long ecommerceId);

    Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId);

    default Mono<OrderCanonical> evaluate(ActionDto actionDto, String ecommerceId) {

        if (!getAction(actionDto.getAction()) ) {

            throw new IllegalArgumentException("Wrong action type " + actionDto + "or not exist");
        }

        if (!validationStatusOrder(Long.parseLong(ecommerceId))) {
            throw new IllegalArgumentException("Status order with order {}" + ecommerceId + "or not exits");
        }

        return process(actionDto, Long.parseLong(ecommerceId));
    }

}
