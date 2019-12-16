package com.inretailpharma.digital.ordermanager.canonical;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderFulfillmentCanonical implements Serializable {

    private Long trackerCode;
    private Long orderId;

    private String status;
    private String statusDetail;

    private String local;
    private String company;

    private String paymentMethod;
    private String leadTime;
    private String documentNumber;
    private BigDecimal totalAmount;


}
