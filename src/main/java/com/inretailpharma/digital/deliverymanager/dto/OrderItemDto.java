package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class OrderItemDto {

    private String productCode;
    private String productName;
    private String shortDescription;
    private String brand;
    private Integer quantity;
    private Integer oldQuantity;
    private Integer modifiedQuantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private BigDecimal modifiedTotalPrice;
    private Boolean fractionated;
    private BigDecimal fractionalDiscount;

    private String productSapCode;
    private String eanCode;
    private Integer presentationId;
    private String presentationDescription;
    private Integer quantityUnits;
    private Integer quantityPresentation;
    private boolean edited;
    private boolean removed;
}
