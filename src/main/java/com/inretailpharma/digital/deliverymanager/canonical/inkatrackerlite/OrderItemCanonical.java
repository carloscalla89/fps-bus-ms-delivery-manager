package com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class OrderItemCanonical {

    @NotNull
    private String productId;
    private String productSapCode;
    @NotNull
    private String name;
    private String shortDescription;
    private String brand;
    @NotNull
    @Min(value = 1, message = "Minimum quantity is 1")
    private Integer quantity;
    @Pattern(regexp = "^(Y|N)$", message = "Y or N")
    private String fractionated;
    @Min(value = 0, message = "UnitPrice cannot be less than 0")
    private Double unitPrice;
    @Min(value = 0, message = "TotalPrice cannot be less than 0")
    private Double totalPrice;
    @Pattern(regexp = "^(Y|N)$", message = "Y or N")
    private String withStock;

}
