package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import java.math.BigInteger;
import java.util.List;
import lombok.Data;

@Data
public class OrderCanonicalResponse {
  private BigInteger page;
  private BigInteger currentRecords;
  private BigInteger totalRecords;
  private List<OrderCanonicalFulfitment> orders;

}
