package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "local")
public class Local {

    /*
    @Id
    private String code;
     */
    @EmbeddedId
    private LocalIdentity localIdentity;
    private String name;

    /*

    @ManyToOne
    @JoinColumn(name = "company_code")
    private Company company;

     */

}
