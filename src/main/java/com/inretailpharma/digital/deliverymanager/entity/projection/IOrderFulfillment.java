package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IOrderFulfillment {

    String getCompany();
    Long getOrderId();
    Long getTrackerId();
    Long getExternalId();

    String getLocalCode();
    String getLocal();
    String getStatusCode();
    String getStatus();
    String getStatusDetail();
    String getPaymentMethod();
    LocalDateTime getLeadTime();
    String getDocumentNumber();
    BigDecimal getTotalAmount();
    Integer getAttempt();
    Integer getAttemptTracker();

    String getServiceTypeCode();
    String getServiceTypeName();

}
