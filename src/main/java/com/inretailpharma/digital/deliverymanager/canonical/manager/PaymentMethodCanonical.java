package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentMethodCanonical {

    private String type;
    private String cardProviderCode;
    private String cardProvider;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private String note;
    private String paymentTransactionId;
    private String purchaseNumber;
    private String numPanVisanet;
    private String transactionDateVisanet;
    private String providerCardCommercialCode;
    private String transactionVisaOrder;
}
