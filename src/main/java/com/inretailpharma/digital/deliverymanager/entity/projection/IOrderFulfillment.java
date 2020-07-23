package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface IOrderFulfillment {

    Long getOrderId();
    Long getEcommerceId();
    Long getTrackerId();
    Long getExternalId();
    Long getBridgePurchaseId();

    BigDecimal getTotalCost();
    BigDecimal getDeliveryCost();


    LocalDateTime getCreatedOrder();
    LocalDateTime getScheduledTime();
    LocalDateTime getConfirmedSchedule();
    LocalDateTime getConfirmedOrder();

    String getFirstName();
    String getLastName();
    String getEmail();
    String getDocumentNumber();
    String getPhone();
    String getBirthDate();
    String getAnonimous();
    Integer getInkaClub();

    String getCenterCode();
    String getCenterName();
    String getCompanyCode();
    String getCompanyName();

    Integer getLeadTime();
    LocalTime getStartHour();
    LocalTime getEndHour();

    String getStatusCode();
    Integer getAttempt();
    Integer getAttemptTracker();
    String getStatusType();
    String getStatusDetail();

    String getServiceTypeCode();
    String getServiceTypeName();
    String getServiceType();

    String getPaymentType();
    String getCardProvider();
    BigDecimal getPaidAmount();
    BigDecimal getChangeAmount();

    String getReceiptType();
    String getDocumentNumberReceipt();
    String getRuc();
    String getCompanyNameReceipt();
    String getCompanyAddressReceipt();
    String getNoteReceipt();

    String getAddressName();
    String getStreet();
    String getNumber();
    String getApartment();
    String getCountry();
    String getCity();
    String getDistrict();
    String getProvince();
    String getDepartment();
    String getNotes();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    String getSource();
    String getOrderNotes();

}
