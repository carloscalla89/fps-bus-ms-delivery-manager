package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class PaymentAdapter implements IPaymentAdapter{

    private OrderExternalService liquidationService;

    @Autowired
    public PaymentAdapter(@Qualifier("liquidation") OrderExternalService liquidationService) {
        this.liquidationService = liquidationService;
    }

    @Override
    public Mono<OrderCanonical> getfromOnlinePayment(IOrderFulfillment iOrderFulfillment, ActionDto actionDto) {

        return liquidationService
                .updateOrderToLiquidationOnline(String.valueOf(iOrderFulfillment.getEcommerceId()), new StatusDto(actionDto.getAction()))
                .map(r -> {
                    log.info("[START] to update online payment order = {}", r);
                    return r;
                });
    }
}
