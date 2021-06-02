package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
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
    private BigDecimal fractionatedPrice;

    private String productSapCode;
    private String eanCode;
    private Integer presentationId;
    private String presentationDescription;
    private Integer quantityUnits;
    private Integer quantityUnitMinimium;
    private Integer quantityPresentation;
    private Integer valueUMV;
    private String familyType;
    private boolean edited;
    private boolean removed;

    /**
     * Fecha: 15/04/2021
     * autor: Equipo Growth
     * Campos referentes a 3 precios
     */
    private BigDecimal priceList;
    private BigDecimal totalPriceList;
    private BigDecimal priceAllPaymentMethod;
    private BigDecimal totalPriceAllPaymentMethod;
    private BigDecimal priceWithpaymentMethod;
    private BigDecimal totalPriceWithpaymentMethod;
    private boolean crossOutPL;
    private String paymentMethodCardType;
    private BigDecimal promotionalDiscount;
}
