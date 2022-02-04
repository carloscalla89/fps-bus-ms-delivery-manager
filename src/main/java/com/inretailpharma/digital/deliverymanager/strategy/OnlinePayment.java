package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.LiquidationDto.StatusDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static com.inretailpharma.digital.deliverymanager.util.Constant.OrderStatus.SUCCESS_RESULT_ONLINE_PAYMENT;

@Slf4j
@Component("payment")
public class OnlinePayment extends FacadeAbstractUtil implements IActionStrategy {

    private OrderExternalService liquidationService;

    public OnlinePayment() {

    }

    @Autowired
    public OnlinePayment(@Qualifier("liquidation") OrderExternalService liquidationService) {

        this.liquidationService = liquidationService;
    }

    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {

        return  Optional.ofNullable(getOnlyOrderByecommerceId(ecommerceId)).isPresent();

    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {

        return liquidationService
                .updateOrderToLiquidationOnline(String.valueOf(ecommerceId), new StatusDto(actionDto.getAction()))
                .map(r -> {
                    log.info("[START] to update online payment order = {}", r);
                    return r;
                });

    }
}
