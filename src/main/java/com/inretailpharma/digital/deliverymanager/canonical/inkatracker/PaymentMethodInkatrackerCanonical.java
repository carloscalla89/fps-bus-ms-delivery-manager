package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class PaymentMethodInkatrackerCanonical {

    private String type;
    private Double paidAmount;
    private Double changeAmount;
    private String provider;
    private String cardName;
    private String cardNumber;
    private String note;
    private String cardCompany;
    private Integer purchaseNumber;
}
