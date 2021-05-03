package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.IDeliveryDispatcherAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class FillOrder implements IActionStrategy {

    @Autowired
    private IDeliveryDispatcherAdapter iDeliveryDispatcherAdapter;
    @Autowired
    private DeliveryManagerFacade deliveryManagerFacade;

    public FillOrder() {

    }
    @Autowired
    public FillOrder(IDeliveryDispatcherAdapter iDeliveryDispatcherAdapter, DeliveryManagerFacade deliveryManagerFacade) {
        this.iDeliveryDispatcherAdapter = iDeliveryDispatcherAdapter;
        this.deliveryManagerFacade = deliveryManagerFacade;
    }

    @Override
    public boolean getAction(String action) {

        return Constant.ActionOrder.FILL_ORDER.name().equalsIgnoreCase(action);
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {
        // action to fill order from ecommerce
        log.info("Action to fill order {} from ecommerce:", ecommerceId);

        return iDeliveryDispatcherAdapter
                .getOrderEcommerce(
                        ecommerceId, Optional.ofNullable(actionDto.getCompanyCode()).orElse(Constant.COMPANY_CODE_IFK))
                .flatMap(or -> deliveryManagerFacade.createOrder(or))
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.NOT_FOUND_ORDER.getCode(),
                                Constant.OrderStatus.NOT_FOUND_ORDER.name()));
    }
}
