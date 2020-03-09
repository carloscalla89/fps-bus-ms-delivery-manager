package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentMethodCanonical {

    private String type;
    private String cardProvider;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
}
