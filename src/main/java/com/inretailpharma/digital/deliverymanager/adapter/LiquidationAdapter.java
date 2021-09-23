package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.LiquidationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LiquidationAdapter extends AdapterAbstractUtil implements ILiquidationAdapter {

    private OrderExternalService liquidationExternalService;

    @Autowired
    public LiquidationAdapter(@Qualifier("liquidation") OrderExternalService liquidationExternalService) {
        this.liquidationExternalService = liquidationExternalService;
    }

    @Override
    public Mono<OrderCanonical> createOrder(OrderCanonical completeOrder, LiquidationCanonical liquidationCanonical) {

        return Mono
                .just(getLiquidationDtoFromOrderCanonical(completeOrder, liquidationCanonical))
                .flatMap(result ->
                        liquidationExternalService
                                .createOrderToLiquidation(result)
                                .flatMap(order -> {

                                    completeOrder.setLiquidation(
                                            LiquidationCanonical
                                                    .builder()
                                                    .code(order.getOrderStatus().getCode())
                                                    .status(order.getOrderStatus().getName())
                                                    .detail(order.getOrderStatus().getDetail())
                                                    .build()
                                    );

                                    return Mono.just(completeOrder);

                                }).onErrorResume(e -> {
                                    e.printStackTrace();
                                    log.error("Error during processing to create order to liquidation:{}",e.getMessage());
                                    completeOrder.setLiquidation(
                                            LiquidationCanonical
                                                    .builder()
                                                    .code(Constant.LiquidationStatus.ERROR_SENDING_CREATE_STATUS.getCode())
                                                    .status(Constant.LiquidationStatus.ERROR_SENDING_CREATE_STATUS.name())
                                                    .detail(e.getMessage())
                                                    .build()
                                    );

                                    return Mono.just(completeOrder);

                                })
                );

    }

    @Override
    public Mono<OrderCanonical> updateOrder(OrderCanonical orderCanonical,LiquidationCanonical liquidationCanonical) {

        return Mono
                .just(getStatusLiquidation(liquidationCanonical, orderCanonical))
                .flatMap(statusDto ->
                        liquidationExternalService
                                .updateOrderToLiquidation(orderCanonical.getEcommerceId().toString(),statusDto)
                                .flatMap(order -> {

                                    orderCanonical.setLiquidation(
                                            LiquidationCanonical
                                                    .builder()
                                                    .code(order.getOrderStatus().getCode())
                                                    .status(order.getOrderStatus().getName())
                                                    .detail(order.getOrderStatus().getDetail())
                                                    .build()
                                    );

                                    return Mono.just(orderCanonical);

                                }).onErrorResume(e -> {

                                    orderCanonical.setLiquidation(
                                            LiquidationCanonical
                                                    .builder()
                                                    .code(Constant.LiquidationStatus.ERROR_UPDATING_STATUS.getCode())
                                                    .status(Constant.LiquidationStatus.ERROR_UPDATING_STATUS.name())
                                                    .detail(e.getMessage())
                                                    .build()
                                    );

                                    return Mono.just(orderCanonical);

                                })
                );
    }
}
