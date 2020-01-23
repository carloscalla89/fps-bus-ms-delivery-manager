package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;


public class LocalIdentity implements Serializable {


    private String code;

    private Company company;

}
