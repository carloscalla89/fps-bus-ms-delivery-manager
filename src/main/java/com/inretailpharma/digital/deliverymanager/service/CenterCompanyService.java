package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.CenterCompanyCanonical;
import reactor.core.publisher.Mono;

public interface CenterCompanyService {
	
	Mono<StoreCenterCanonical> getExternalInfo(String localcode);
	Mono<StoreCenterCanonical> getExternalInfo(String companyCode, String localcode);

}
