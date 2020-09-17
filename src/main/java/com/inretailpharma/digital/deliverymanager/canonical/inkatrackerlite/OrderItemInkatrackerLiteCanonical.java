package com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemInkatrackerLiteCanonical {

    private String productId;
    private String productSapCode;
    private String name;
    private String shortDescription;
    private String brand;
    private Integer quantity;
    private String fractionated;
    private Double unitPrice;
    private Double totalPrice;
    private String withStock;
    private Integer presentationId;
    private String presentationDescription;
    private Integer quantityUnits;
    private Integer quantityPresentation;

}
