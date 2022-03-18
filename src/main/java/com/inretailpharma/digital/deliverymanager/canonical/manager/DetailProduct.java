package com.inretailpharma.digital.deliverymanager.canonical.manager;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class DetailProduct {
  private String sku;
  private Integer quantity;
  private BigDecimal unitPrice;
  private BigDecimal totalPrice;
  private String name;
  //private String shortDescription;
  //TODO: OMS
  private String presentationDescription;
}
