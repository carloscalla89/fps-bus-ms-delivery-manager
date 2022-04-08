package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.math.BigDecimal;

public interface IOrderInfoProductDetail {
   String getSku();
   Integer getQuantity();
   BigDecimal getUnitPrice();
   BigDecimal getTotalPrice();
   String getName();
   //String getShortDescription();
   //TODO: OMS
   String getPresentationDescription();
   BigDecimal getTotalPriceAllPaymentMethod();
   BigDecimal getTotalPriceTOH();
}
