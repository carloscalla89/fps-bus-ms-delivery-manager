package com.inretailpharma.digital.deliverymanager.entity.projection;


import java.math.BigDecimal;

public interface IOrderItemFulfillment {

    String getProductCode();
    String getProductSapCode();
    String getNameProduct();
    String getShortDescriptionProduct();
    String getBrand();
    Integer getQuantity();
    BigDecimal getUnitPrice();
    BigDecimal getTotalPrice();
    String getFractionated();
}
