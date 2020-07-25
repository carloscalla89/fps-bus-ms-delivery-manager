package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class ReceiptDto {

    private String ruc;
    private String companyName;
    private String companyAddress;
    @NotNull
    @Valid
    private ReceiptTypeDto receiptType;

    @Override
    public String toString() {
        return "ReceiptDto{" +
                "ruc='" + ruc + '\'' +
                ", companyName='" + companyName + '\'' +
                ", companyAddress='" + companyAddress + '\'' +
                ", receiptType=" + receiptType +
                '}';
    }
}
