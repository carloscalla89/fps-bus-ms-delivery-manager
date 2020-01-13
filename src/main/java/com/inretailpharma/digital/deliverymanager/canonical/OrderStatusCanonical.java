package com.inretailpharma.digital.deliverymanager.canonical;

import lombok.Data;

import java.io.Serializable;

@Data
public class OrderStatusCanonical implements Serializable {

    private String status;
    private String statusCode;
    private String statusDetail;

}
