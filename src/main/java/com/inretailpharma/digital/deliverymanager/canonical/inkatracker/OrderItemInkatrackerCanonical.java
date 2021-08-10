package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemInkatrackerCanonical {

    private String sku;
    private String productId;
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
    private Integer quantityUnitMinimium;
    private Integer valueUMV;

    //3 precios
    private Double priceList;
    private Double totalPriceList;
    private Double priceAllPaymentMethod;
    private Double totalPriceAllPaymentMethod;
    private Double priceWithpaymentMethod;
    private Double totalPriceWithpaymentMethod;
    private boolean crossOutPL;
    private String paymentMethodCardType;

}
