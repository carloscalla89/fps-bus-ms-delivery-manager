package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;

@Data
public class ItemDto {

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$")
    private String productId;
    @NotBlank
    @Pattern(regexp = "^[0-9]*$")
    private String productSapCode;
    @NotBlank
    private String name;
    @NotNull
    private String shortDescription;
    @NotNull
    private String brand;
    @NotNull
    @Min(1)
    private Integer quantity;
    @NotNull
    @Min(0)
    private Integer quantityUnits;
    @NotNull
    @Pattern(regexp = "^[YN]$")
    private String fractionated;
    @NotNull
    @Min(0)
    private BigDecimal unitPrice;
    @NotNull
    @Min(0)
    private BigDecimal totalPrice;
    @NotNull
    private String presentation;
    @NotNull
    private String familyType;
    @NotNull
    private String eanCode;
    @NotNull
    @Min(0)
    private Double fractionatedPrice;
    @NotNull
    private Integer presentationType;
    private BigDecimal fractionalDiscount;

}
