package com.inretailpharma.digital.deliverymanager.canonical;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderStatusCanonical implements Serializable {

    private String code;
    private String name;
    private String detail;

}
