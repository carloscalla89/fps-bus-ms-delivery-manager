package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class OrderItemInkatrackerCanonical {

    private String sku;
    private String sap;
    private String name;
    private String shortDescription;
    private String brand;
    private Integer quantity;
    private String fractionated;
    private Double unitPrice;
    private Double totalPrice;
    private String withStock;
    private String eanCode;
    private Integer presentationId;
    private String presentationDescription;
    private boolean prescription;
    private Integer quantityUnits;
    private Integer quantityPresentation;

}
