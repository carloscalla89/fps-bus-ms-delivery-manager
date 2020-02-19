package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

@Data
public class OrderWrapperResponse {

    private Long trackerId;
    private String orderStatusCode;
    private String orderStatusName;
    private String orderStatusDetail;
    private String serviceCode;
    private String serviceType;
    private String serviceName;
    private Integer attemptBilling;
    private Integer attemptTracker;
    private String localCode;
    private String localName;
    private String companyCode;
    private String companyName;
    private String receiptName;
    private String paymentMethodName;

}
