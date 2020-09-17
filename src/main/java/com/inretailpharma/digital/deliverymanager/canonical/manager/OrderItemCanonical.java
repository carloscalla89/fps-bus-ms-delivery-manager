package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
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
    private BigDecimal fractionatedPrice;
    private Integer presentationId;
    private String presentationDescription;
    private Integer quantityUnits;
    private Integer quantityUnitMinimium;
    private Integer quantityPresentation;


}
