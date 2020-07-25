package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentMethodCanonical {
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
