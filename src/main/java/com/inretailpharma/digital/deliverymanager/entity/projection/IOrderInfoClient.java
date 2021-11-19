package com.inretailpharma.digital.deliverymanager.entity.projection;

public interface IOrderInfoClient {

  Long getOrderId();

  Long getEcommerceId();

  String getCompanyCode();

  String getServiceChannel();

  String getOrderType();

  String getServiceTypeShortCode();

  String getScheduledTime();

  String getStatusName();

  String getLocalCode();

  String getClientName();

  Long getDocumentNumber();

  String getPhone();

  String getEmail();

  String getAddressClient();

  String getCoordinates();

  String getReference();

}
