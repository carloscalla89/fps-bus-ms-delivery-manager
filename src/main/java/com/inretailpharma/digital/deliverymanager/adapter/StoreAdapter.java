package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class StoreAdapter implements IStoreAdapter {

    private OrderExternalService orderExternalService;

    @Autowired
    public StoreAdapter(@Qualifier("store") OrderExternalService orderExternalService) {
        this.orderExternalService = orderExternalService;

    }

    @Override
    public Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localcode) {
        return orderExternalService.getStoreByCompanyCodeAndLocalCode(companyCode, localcode);
    }
}
