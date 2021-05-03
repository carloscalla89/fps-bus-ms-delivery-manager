package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.UtilFunctions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LiquidationAdapter extends AdapterAbstractUtil implements ILiquidationAdapter {

    @Override
    public Mono<OrderCanonical> createOrder(OrderCanonical orderCanonical) {

        return null;
    }

    @Override
    public Mono<OrderCanonical> updateOrder(OrderCanonical orderCanonical, String action) {

        return null;
    }
}
