package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class PaymentMethodDto {

    private String type;
    private String cardProvider;
    private Integer cardProviderId;
    private String cardProviderCode;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private String bin;
    private String coupon;
    private String providerCardCommercialCode;
    private String paymentTransactionId;
    private String numPanVisaNet;
    private String transactionDateVisaNet;
}
