package com.inretailpharma.digital.deliverymanager.service;

import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;

public interface ApplicationParameterService {

    ApplicationParameter findApplicationParameterByCode(String code);
    ApplicationParameter getApplicationParameterByCodeIs(String code);
}