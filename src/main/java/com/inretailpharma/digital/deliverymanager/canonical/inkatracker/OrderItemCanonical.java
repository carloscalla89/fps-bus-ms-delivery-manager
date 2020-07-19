package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemCanonical {
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
    private Integer quantityUnits;
    private Integer quantityPresentation;

}
