package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;

@Data
@Embeddable
public class LocalIdentity implements Serializable {


    private String code;

    @ManyToOne
    @JoinColumn(name = "company_code")
    private Company company;

}
