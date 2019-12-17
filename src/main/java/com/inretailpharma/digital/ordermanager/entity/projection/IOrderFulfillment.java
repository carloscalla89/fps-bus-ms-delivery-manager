package com.inretailpharma.digital.ordermanager.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IOrderFulfillment {

    String getCompany();
    Long getOrderId();
    String getLocalCode();
    String getLocal();
    String getStatus();
    String getStatusDetail();
    String getPaymentMethod();
    LocalDateTime getLeadTime();
    String getDocumentNumber();
    BigDecimal getTotalAmount();

}
