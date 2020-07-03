package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CenterCompanyCanonical;

public interface CenterCompanyService {
	
	CenterCompanyCanonical getExternalInfo(String localcode);

}
