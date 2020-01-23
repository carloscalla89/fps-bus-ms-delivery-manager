package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "center_company_fulfillment")
public class CenterCompanyFulfillment {

    @EmbeddedId
    private CenterCompanyIdentity centerCompanyIdentity;

    @Column(name="center_name")
    private String centerName;

    @Column(name="company_name")
    private String companyName;
}
