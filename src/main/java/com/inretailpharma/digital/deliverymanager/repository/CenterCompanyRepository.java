package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.CenterCompanyFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.CenterCompanyIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenterCompanyRepository extends JpaRepository<CenterCompanyFulfillment, CenterCompanyIdentity> {

    CenterCompanyFulfillment getCenterCompanyFulfillmentByCenterCompanyIdentity_CenterCodeAndCenterCompanyIdentity_CompanyCode(
            String centerCode, String companyCode);
}
