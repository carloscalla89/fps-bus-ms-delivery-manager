package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CancellationCodeReasonRepository extends JpaRepository<CancellationCodeReason, String>{

    List<CancellationCodeReason> findAllByAppTypeIn(List<String> appType);
    List<CancellationCodeReason> findAllByCode(String code);
    CancellationCodeReason findByCodeAndAppType(String code, String appType);
    List<CancellationCodeReason> findAllByAppTypeInAndType(List<String> appType, String type);
}
