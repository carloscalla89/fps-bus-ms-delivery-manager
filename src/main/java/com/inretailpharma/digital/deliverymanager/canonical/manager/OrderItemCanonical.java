package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class OrderItemCanonical {

    private String sku;
    private String skuSap;
    private String skuName;
    private String productEan;
    private String shortDescription;
    private String brand;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean fractionated;
    private BigDecimal fractionalDiscount;
    private BigDecimal fractionatedPrice;
    private Integer presentationId;
    private String presentationDescription;
    private Integer quantityUnits;
    private Integer quantityUnitMinimium;
    private Integer quantityPresentation;
    private Integer valueUMV;



}
