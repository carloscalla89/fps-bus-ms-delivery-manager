package com.inretailpharma.digital.deliverymanager.repository;

import com.inretailpharma.digital.deliverymanager.entity.Local;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocalRepository extends JpaRepository<Local, String> {

    Local getLocalByLocalIdentityCodeAndLocalIdentityCompany_Code(String localCode, String companyCode);

}
