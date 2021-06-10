package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("liquidation")
public class LiquidationOrder extends FacadeAbstractUtil implements IActionStrategy {

    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {
        return true;
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {
        return null;
    }
}
