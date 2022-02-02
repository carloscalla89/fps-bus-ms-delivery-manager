package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class FiltersRqDTO {

  private String[] ecommerceId;
  private String[] localId;
  private String[] companyCode;
  private String[] serviceTypeId;
  private String[] promiseDate;
  private String[] orderStatus;
  private String[] serviceChannel;
  //TODO: OMS
  //private String multipleField;
  private String filterType;
  private String valueFilterType; //(1)N° pedido, (2)telefono o (3)documento
}
