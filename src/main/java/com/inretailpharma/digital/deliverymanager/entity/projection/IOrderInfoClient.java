package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.time.LocalDateTime;

public interface IOrderInfoClient {

  Long getOrderId();

  Long getEcommerceId();

  String getCompanyCode();

  String getServiceChannel();

  String getOrderType();

  String getServiceTypeShortCode();

  LocalDateTime getScheduledTime();

  String getStatusName();

  String getLocalCode();

  String getClientName();

  Long getDocumentNumber();

  String getPhone();

  String getEmail();

  String getAddressClient();

  String getCoordinates();

  String getReference();

  String getServiceType();

  String getPurcharseId();

  String getObservation();

  String getCancelReason();

  String getZoneId();

  String getStockType();

}
