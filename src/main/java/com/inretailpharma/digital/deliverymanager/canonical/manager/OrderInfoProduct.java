package com.inretailpharma.digital.deliverymanager.canonical.manager;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import lombok.Data;

@Data
public class OrderInfoProduct {
  private BigInteger id;
  private BigDecimal totalImport;
  private BigDecimal totalImportTOH;
  private BigDecimal totalDiscount;
  private BigDecimal deliveryAmount;
  private BigDecimal totalImportWithOutDiscount;
  private List<DetailProduct> products;

}
