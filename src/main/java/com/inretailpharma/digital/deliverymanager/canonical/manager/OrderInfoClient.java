package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class OrderInfoClient {


  private String clientName;
  private Long documentNumber;
  private String phone;
  private String email;
  private String addressClient;
  private String coordinates;
  private String reference;
  private String companyCode;
  //TODO: OMS
  private String ruc;
  private String companyName;
}
