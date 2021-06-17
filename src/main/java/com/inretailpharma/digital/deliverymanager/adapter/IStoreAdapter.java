package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import reactor.core.publisher.Mono;

public interface IStoreAdapter {

    Mono<StoreCenterCanonical> getStoreByCompanyCodeAndLocalCode(String companyCode, String localcode);
}
