package com.inretailpharma.digital.deliverymanager.entity.projection;

import com.inretailpharma.digital.deliverymanager.canonical.manager.DetailProduct;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public interface IOrderInfoProduct {

   BigInteger getId();
   BigDecimal getTotalImport();
   BigDecimal getTotalDiscount();
   BigDecimal getDeliveryAmount();
   BigDecimal getTotalImportWithOutDiscount();
}
