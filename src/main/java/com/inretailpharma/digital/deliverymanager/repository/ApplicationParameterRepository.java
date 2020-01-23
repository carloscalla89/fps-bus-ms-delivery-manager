package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationParameterRepository extends JpaRepository<ApplicationParameter, String> {

    ApplicationParameter getApplicationParameterByCodeIs(String code);
}
