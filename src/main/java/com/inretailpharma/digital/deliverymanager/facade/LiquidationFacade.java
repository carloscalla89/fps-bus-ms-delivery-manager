package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.adapter.LiquidationAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.LiquidationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
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
                    .just(orderCanonical)
                    .flatMap(order -> Mono.just(getLiquidationStatusByDigitalStatusCode(order.getOrderStatus().getCode())))
                    .defaultIfEmpty(LiquidationCanonical.builder().enabled(false).build())
                    .filter(LiquidationCanonical::getEnabled)
                    .flatMap(result -> iLiquidationAdapter
                            .createOrder(completeOrder, result)
                            .flatMap(resultOrder -> {

                                orderTransaction.updateLiquidationStatusOrder(
                                        resultOrder.getLiquidation().getStatus(), resultOrder.getLiquidation().getDetail(), orderCanonical.getId()
                                );

                                return Mono.just(resultOrder);
                            })
                    )
                    .defaultIfEmpty(orderCanonical);

        } else {
            return Mono.just(orderCanonical);
        }

    }

    public Mono<OrderCanonical> evaluateUpdate(OrderCanonical orderCanonical, ActionDto actionDto) {

        if (getValueBoolenOfParameter() && !actionDto.getAction().equalsIgnoreCase(Constant.ActionOrder.LIQUIDATE_ORDER.name())) {

            return Mono
                    .just(orderCanonical)
                    .flatMap(order -> Mono.just(getLiquidationStatusByDigitalStatusCode(order.getOrderStatus().getCode())))
                    .defaultIfEmpty(LiquidationCanonical.builder().enabled(false).build())
                    .filter(LiquidationCanonical::getEnabled)
                    .flatMap(result -> {
                        log.info("evaluateUpdate liquidation result:{}",result);
                        if (result.getStatus() == null) {

                            StatusDto liquidationStatus = UtilFunctions.processLiquidationStatus.process(
                                    result.getStatus(), orderCanonical.getOrderStatus().getFirstStatusName(),
                                    actionDto.getAction(), orderCanonical.getOrderStatus().getCancellationCode(), orderCanonical.getOrderDetail().getServiceType());

                            result.setCode(liquidationStatus.getCode());
                            result.setStatus(liquidationStatus.getName());
                        }

                        return Mono.just(result);
                    })
                    .flatMap(result ->
                            iLiquidationAdapter
                                    .updateOrder(orderCanonical, result)
                                    .flatMap(resultOrder -> {

                                        orderTransaction.updateLiquidationStatusOrder(
                                                resultOrder.getLiquidation().getStatus(), resultOrder.getLiquidation().getDetail(), orderCanonical.getId()
                                        );

                                        return Mono.just(resultOrder);
                                    })
                    )
                    .defaultIfEmpty(orderCanonical)
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        log.error("Error in process transaction status liquidation:{}, order:{}",e.getMessage(), orderCanonical);

                        return Mono.just(orderCanonical);

                    });

        } else {

            return Mono.just(orderCanonical);
        }

    }

    private boolean getValueBoolenOfParameter() {
        return getValueBoolenOfParameter(Constant.ApplicationsParameters.ENABLED_SEND_TO_LIQUIDATION);
    }
}