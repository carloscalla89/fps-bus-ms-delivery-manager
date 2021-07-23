package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.IInsinkAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FillOrderCall extends FacadeAbstractUtil implements IActionStrategy {

    private IInsinkAdapter insinkAdapter;

    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {

        return true;
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {
        // action to fill order from ecommerce
        log.info("Action to fill order {} from ecommerce:", ecommerceId);

        return insinkAdapter
                .getOrderEcommerce(ecommerceId)
                .flatMap(this::createOrderFulfillment)
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.NOT_FOUND_ORDER.getCode(),
                                Constant.OrderStatus.NOT_FOUND_ORDER.name()));
    }

    @Autowired
    public void setInsinkAdapter(IInsinkAdapter insinkAdapter) {
        this.insinkAdapter = insinkAdapter;
    }
}
