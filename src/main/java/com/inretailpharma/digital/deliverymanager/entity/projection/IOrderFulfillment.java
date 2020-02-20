package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IOrderFulfillment {

    Long getOrderId();
    Long getEcommerceId();
    Long getTrackerId();
    Long getExternalId();

    LocalDateTime getCreatedOrder();
    LocalDateTime getScheduledTime();

    String getDocumentNumber();
    BigDecimal getTotalAmount();
    String getPaymentMethod();

    String getLocalCode();
    String getLocal();
    String getCompany();

    String getStatusCode();
    String getStatusType();
    String getStatusDetail();

    LocalDateTime getLeadTime();


    Integer getAttempt();
    Integer getAttemptTracker();

    String getServiceTypeCode();
    String getServiceTypeName();

}
