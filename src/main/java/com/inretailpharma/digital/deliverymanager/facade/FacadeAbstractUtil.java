package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
public abstract class FacadeAbstractUtil {

    @Autowired
    private OrderTransaction orderTransaction;

    protected Mono<OrderCanonical> updateOrderInfulfillment(OrderCanonical orderCanonical, Long id, Long ecommerceId,
                                                            Long externalId, String orderCancelCode,
                                                            String orderCancelObservation, String source, String target,
                                                            String updateBy, String actionDate) {

        log.info("Target to send:{}, updateBy:{}",target,updateBy);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);
        orderCanonical.setUpdateBy(updateBy);

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation, orderCancelCode,
                orderCanonical.getOrderStatus().getCode(), id, localDateTime,
                Optional.ofNullable(orderCancelCode).map(oc -> localDateTime).orElse(null)
        );

        orderCanonical.setExternalId(externalId);
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        orderCanonical.getOrderStatus().setCancellationCode(orderCancelCode);
        orderCanonical.getOrderStatus().setCancellationObservation(orderCancelObservation);

        return Mono.just(orderCanonical);

    }

    protected Mono<OrderCanonical> updateOrderInfulfillment(OrderCanonical orderCanonical,Long ecommerceId,
                                                            String source, String target, String updateBy, String actionDate) {

        log.info("Target to send:{}, updateBy:{}",target,updateBy);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);
        orderCanonical.setUpdateBy(updateBy);

        orderTransaction.updateStatusOrder(
                ecommerceId, orderCanonical.getOrderStatus().getCode(), orderCanonical.getOrderStatus().getDetail(),
                localDateTime
        );

        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        return Mono.just(orderCanonical);

    }
}
