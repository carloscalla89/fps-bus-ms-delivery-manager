package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
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

    @Override
    public String toString() {
        return "PaymentDto{" +
                "productsTotalCostNoDiscount=" + productsTotalCostNoDiscount +
                ", grossPrice=" + grossPrice +
                ", discountApplied=" + discountApplied +
                ", productsTotalCost=" + productsTotalCost +
                ", deliveryCost=" + deliveryCost +
                '}';
    }
}
