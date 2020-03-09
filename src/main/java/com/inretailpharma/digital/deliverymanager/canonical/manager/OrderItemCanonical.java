package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemCanonical {

    private String productCode;
    private String productName;
    private String shortDescription;
    private String brand;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Boolean fractionated;


}
