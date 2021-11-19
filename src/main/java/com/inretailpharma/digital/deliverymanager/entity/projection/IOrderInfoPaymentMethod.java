package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.math.BigDecimal;

public interface IOrderInfoPaymentMethod {

  String getPaymentType();

  String getTransactionId();

  String getPaymentGateway();

  BigDecimal getChangeAmount();

  String getLiquidationStatus();

  String getDateConfirmed();

  String getCardNumber();

  String getCodAuthorization();

  String getCardBrand();

  String getFinancial();

  String getLiquidatorUser();

  String getServiceTypeCode();

}
