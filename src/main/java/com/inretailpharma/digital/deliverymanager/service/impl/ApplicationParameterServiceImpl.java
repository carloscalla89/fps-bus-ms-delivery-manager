package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.repository.ApplicationParameterRepository;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import org.springframework.stereotype.Service;

@Service
public class ApplicationParameterServiceImpl implements ApplicationParameterService {

    private ApplicationParameterRepository applicationParameterRepository;

    public ApplicationParameterServiceImpl(ApplicationParameterRepository applicationParameterRepository) {
        this.applicationParameterRepository = applicationParameterRepository;
    }

    @Override
    public ApplicationParameter findApplicationParameterByCode(String code) {
        return applicationParameterRepository.getOne(code);
    }

    @Override
    public ApplicationParameter getApplicationParameterByCodeIs(String code) {
        return applicationParameterRepository.getApplicationParameterByCodeIs(code);
    }
}
