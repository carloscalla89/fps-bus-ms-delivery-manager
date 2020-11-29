package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PaymentDto {

    @NotNull
    @Min(0)
    private BigDecimal productsTotalCostNoDiscount;
    @NotNull
    @Min(0)
    private BigDecimal grossPrice;
    @NotNull
    private BigDecimal discountApplied;
    @NotNull
    @Min(0)
    private BigDecimal productsTotalCost;
    @NotNull
    @Min(0)
    private BigDecimal deliveryCost;

    private String coupon;

    private BigDecimal amount;

}
