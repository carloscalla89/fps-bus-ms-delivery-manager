package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Embeddable
public class CenterCompanyIdentity implements Serializable {

    @Column(name="center_code")
    private String centerCode;

    @Column(name="company_code")
    private String companyCode;

}
