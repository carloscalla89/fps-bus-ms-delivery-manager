package com.inretailpharma.digital.ordermanager.canonical;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderStatusErrorCanonical implements Serializable {

    private String localCode;
    private String local;
    private String company;

    private Long orderId;
    private String status;

    private String errorType;
    private String errorTypeDescription;

    private String paymentMethod;

    private String leadTime;

    private String documentNumber;
    private BigDecimal totalAmount;





}
