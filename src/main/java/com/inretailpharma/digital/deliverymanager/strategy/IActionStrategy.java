package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
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

            OrderStatusCanonical os = new OrderStatusCanonical();
            os.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            os.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
            os.setDetail("The order " + ecommerceId + " not exits or has a final status");
            os.setStatusDate(DateUtils.getLocalDateTimeNow());

            OrderCanonical resultOrderNotFound = new OrderCanonical();

            resultOrderNotFound.setOrderStatus(os);
            resultOrderNotFound.setEcommerceId(Long.parseLong(ecommerceId));

            return Mono.just(resultOrderNotFound);

        }

        return process(actionDto, Long.parseLong(ecommerceId));
    }

}
