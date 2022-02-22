package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component
public class BillOrder extends FacadeAbstractUtil implements IActionStrategy{

    public BillOrder() {}

    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {
        return  Optional
                .ofNullable(getOnlyOrderByecommerceId(ecommerceId))
                .isPresent();
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {

        return updateVoucher(ecommerceId, true)
                .flatMap(r -> {
                    //audit
                    return Mono.just(new OrderCanonical(ecommerceId
                            , Constant.OrderStatus.BILLED_ORDER.getCode()
                            , Constant.OrderStatus.BILLED_ORDER.name()));
                });

    }
}
