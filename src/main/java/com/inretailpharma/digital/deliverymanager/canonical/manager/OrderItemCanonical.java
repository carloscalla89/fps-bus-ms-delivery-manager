package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemCanonical {

    private String productCode;
    private String productName;
    private String productEan;
    private String shortDescription;
    private String brand;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean fractionated;
    private BigDecimal fractionalDiscount;
    private Integer presentationId;
    private String presentationDescription;
    private Integer quantityUnits;
    private Integer quantityPresentation;


}
