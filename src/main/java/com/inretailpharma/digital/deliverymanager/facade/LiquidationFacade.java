package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.adapter.LiquidationAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.LiquidationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.UtilFunctions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LiquidationFacade extends FacadeAbstractUtil {

    private LiquidationAdapter iLiquidationAdapter;
    private OrderTransaction orderTransaction;

    @Autowired
    public LiquidationFacade(LiquidationAdapter iLiquidationAdapter, OrderTransaction orderTransaction) {
        this.iLiquidationAdapter = iLiquidationAdapter;
        this.orderTransaction = orderTransaction;
    }

    public Mono<OrderCanonical> create(OrderCanonical orderCanonical, OrderCanonical completeOrder) {

        if (getValueBoolenOfParameter()) {

            return Mono
                    .just(getLiquidationStatusByDigitalStatusCode(orderCanonical.getOrderStatus().getCode()))
                    .defaultIfEmpty(LiquidationCanonical.builder().enabled(false).build())
                    .filter(LiquidationCanonical::isEnabled)
                    .flatMap(result -> iLiquidationAdapter.createOrder(completeOrder, result))
                    .defaultIfEmpty(orderCanonical);

        } else {
            return Mono.just(orderCanonical);
        }

    }

    public Mono<OrderCanonical> evaluateUpdate(OrderCanonical orderCanonical, String action) {

        if (getValueBoolenOfParameter()) {

            return Mono
                    .just(orderCanonical)
                    .filter(order -> order.getOrderStatus().isSuccessful())
                    .flatMap(order -> Mono.just(getLiquidationStatusByDigitalStatusCode(order.getOrderStatus().getCode())))
                    .defaultIfEmpty(LiquidationCanonical.builder().enabled(false).build())
                    .filter(LiquidationCanonical::isEnabled)
                    .flatMap(result -> {

                        if (result.getStatus() == null) {

                            String liquidationStatus = UtilFunctions.processLiquidationStatus.process(
                                    result.getStatus(), orderCanonical.getOrderStatus().getFirstStatusName(),
                                    action, orderCanonical.getOrderStatus().getCancellationCode(), orderCanonical.getOrderDetail().getServiceType());

                            Constant.OrderStatusLiquidation orderStatusLiquidation = Constant.OrderStatusLiquidation.getStatusByName(liquidationStatus);
                            result.setCode(orderStatusLiquidation.getCode());
                            result.setStatus(orderStatusLiquidation.name());
                        }

                        return Mono.just(result);
                    })
                    .flatMap(result -> iLiquidationAdapter.updateOrder(orderCanonical, result))
                    .defaultIfEmpty(orderCanonical);
        } else {

            return Mono.just(orderCanonical);
        }

    }

    private boolean getValueBoolenOfParameter() {
        return getValueBoolenOfParameter(Constant.ApplicationsParameters.ENABLED_SEND_TO_LIQUIDATION);
    }
}
