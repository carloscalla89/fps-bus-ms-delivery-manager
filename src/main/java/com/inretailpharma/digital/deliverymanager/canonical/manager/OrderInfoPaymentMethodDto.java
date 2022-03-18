package com.inretailpharma.digital.deliverymanager.canonical.manager;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class OrderInfoPaymentMethodDto {

  private String paymentType;
  private String transactionId;
  private String paymentGateway;
  private BigDecimal changeAmount;
  private String liquidationStatus;
  private String paymentDate;
  private String cardNumber;
  private String codAuthorization;
  private String cardBrand;
  private String financial;
  private String liquidatorUser;
  private String serviceTypeCode;
}
