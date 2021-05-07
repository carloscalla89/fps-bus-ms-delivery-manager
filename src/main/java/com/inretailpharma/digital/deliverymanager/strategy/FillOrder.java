package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.IDeliveryDispatcherAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.IStoreAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ITrackerAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class FillOrder extends FacadeAbstractUtil implements IActionStrategy {

    @Autowired
    private IDeliveryDispatcherAdapter iDeliveryDispatcherAdapter;


    @Override
    public boolean getAction(String action) {

        return Constant.ActionOrder.FILL_ORDER.name().equalsIgnoreCase(action);
    }

    @Override
    public boolean validationStatusOrder(Long ecommerceId) {
        return getOnlyOrderStatusByecommerceId(ecommerceId);
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {
        // action to fill order from ecommerce
        log.info("Action to fill order {} from ecommerce:", ecommerceId);

        return iDeliveryDispatcherAdapter
                .getOrderEcommerce(
                        ecommerceId, Optional.ofNullable(actionDto.getCompanyCode()).orElse(Constant.COMPANY_CODE_IFK))
                .flatMap(this::createOrderFulfillment)
                .defaultIfEmpty(
                        new OrderCanonical(
                                ecommerceId,
                                Constant.OrderStatus.NOT_FOUND_ORDER.getCode(),
                                Constant.OrderStatus.NOT_FOUND_ORDER.name()));
    }
}
