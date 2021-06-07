package com.inretailpharma.digital.deliverymanager.entity.projection;


import java.math.BigDecimal;

public interface IOrderItemFulfillment {

    Long getOrderFulfillmentId();
    String getProductCode();
    String getProductSapCode();
    String getNameProduct();
    String getShortDescriptionProduct();
    String getBrandProduct();
    Integer getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getTotalPrice();
    BigDecimal getFractionalDiscount();
    BigDecimal getFractionatedPrice();
    String getFractionated();
    String getEanCode();
    Integer getPresentationId();
    String getPresentationDescription();
    Integer getQuantityUnits();
    Integer getQuantityUnitMinimium();
    Integer getQuantityPresentation();
    String getFamilyType();
    Integer getValueUmv();

    /**
     * Fecha: 15/04/2021
     * autor: Equipo Growth
     * Campos referentes a 3 precios
     */
    BigDecimal getPriceList();
    BigDecimal getTotalPriceList();
    BigDecimal getPriceAllPaymentMethod();
    BigDecimal getTotalPriceAllPaymentMethod();
    BigDecimal getPriceWithpaymentMethod();
    BigDecimal getTotalPriceWithpaymentMethod();
    boolean getCrossOutPL();
    String getPaymentMethodCardType();
    BigDecimal getPromotionalDiscount();

    String getProductCodeInkafarma();

}
