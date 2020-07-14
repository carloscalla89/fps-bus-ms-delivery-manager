package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CenterCompanyCanonical;
import reactor.core.publisher.Mono;

public interface CenterCompanyService {
	
	Mono<CenterCompanyCanonical> getExternalInfo(String localcode);

}
