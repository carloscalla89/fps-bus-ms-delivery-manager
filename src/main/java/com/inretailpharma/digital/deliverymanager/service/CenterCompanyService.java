package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.CenterCompanyCanonical;

public interface CenterCompanyService {
	
	CenterCompanyCanonical getExternalInfo(String localcode);

}
