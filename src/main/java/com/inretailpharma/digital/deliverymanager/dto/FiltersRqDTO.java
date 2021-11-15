package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

@Data
public class FiltersRqDTO {
  private String ecommerceId;
  private String localId;
  private String companyCode;
  private String serviceTypeId;
  private String promiseDate;
  private String orderStatus;
  private String serviceChannel;
}
