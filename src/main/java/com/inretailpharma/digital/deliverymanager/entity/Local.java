package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;

public class Local {

    /*
    @Id
    private String code;
     */
    private LocalIdentity localIdentity;
    private String name;

    /*

    @ManyToOne
    @JoinColumn(name = "company_code")
    private Company company;

     */

}
