package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IActionStrategy {

    boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto);

    Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId);

    default Mono<OrderCanonical> evaluate(ActionDto actionDto, String ecommerceId) {


        if (!validationIfExistOrder(Long.parseLong(ecommerceId), actionDto)) {

            OrderStatusCanonical os = new OrderStatusCanonical();
            os.setCode(Constant.OrderStatus.NOT_FOUND_ORDER.getCode());
            os.setName(Constant.OrderStatus.NOT_FOUND_ORDER.name());
            os.setDetail("The order " + ecommerceId + " not exist or this has a final status");
            os.setStatusDate(DateUtils.getLocalDateTimeNow());

            OrderCanonical resultOrderNotFound = new OrderCanonical();

            resultOrderNotFound.setOrderStatus(os);
            resultOrderNotFound.setEcommerceId(Long.parseLong(ecommerceId));

            return Mono.just(resultOrderNotFound);

        }

        return process(actionDto, Long.parseLong(ecommerceId));
    }

}
