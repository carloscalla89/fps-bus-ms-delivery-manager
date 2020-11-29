package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PaymentDto {

    @NotNull
    @Min(0)
    private Double productsTotalCostNoDiscount;
    @NotNull
    @Min(0)
    private Double grossPrice;
    @NotNull
    private Double discountApplied;
    @NotNull
    @Min(0)
    private Double productsTotalCost;
    @NotNull
    @Min(0)
    private Double deliveryCost;

    private String coupon;

    private Double amount;

}
