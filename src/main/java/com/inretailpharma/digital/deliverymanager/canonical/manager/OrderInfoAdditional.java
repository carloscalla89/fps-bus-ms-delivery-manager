package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderInfoAdditional {

  private Long ecommerceId;
  private String purchaseId;
  private String operator;
  private String observation;
  private String cancellationReason;
  private String zoneId;
  private String localCode;
  private String serviceType;
  private String stockType;

}
