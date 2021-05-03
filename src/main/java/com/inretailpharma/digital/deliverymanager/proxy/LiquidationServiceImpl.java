package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LiquidationServiceImpl extends AbstractOrderService implements OrderExternalService {


    @Override
    public Mono<OrderCanonical> createOrderToLiquidation(OrderCanonical orderCanonical) {


        return null;
    }

    @Override
    public Mono<OrderCanonical> updateOrderToLiquidation(String status, Long ecommerceId) {
        return null;
    }

}
