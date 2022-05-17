package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderInfo {
  private Long orderId;
  private Long ecommerceId;
  private String companyCode;
  private String serviceChannel;
  private String orderType;
  private String serviceTypeShortCode;
  private String scheduledTime;
  private String statusName;
  private String localCode;
  private String serviceType;
  //TODO: OMS
  private Long ecommerceIdCall;
  private String source;
}
