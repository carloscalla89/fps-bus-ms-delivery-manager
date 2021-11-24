package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderInfoAdditional {

  private Long ecommerceId;
  private String purchaseId;
  private String operator;
  private String observation;
  private String cancellationReason;
  private String zoneDescription;
  private String localDescription;
  private String serviceType;
  private String stockType;

}
