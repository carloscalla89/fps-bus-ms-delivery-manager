package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface IOrderFulfillment {

    Long getOrderId();
    Long getEcommerceId();
    Long getTrackerId();
    Long getExternalId();
    Integer getPurchaseId();


    BigDecimal getTotalCost();
    BigDecimal getSubTotalCost();
    BigDecimal getDeliveryCost();
    BigDecimal getDiscountApplied();

    LocalDateTime getCreatedOrder();
    LocalDateTime getScheduledTime();
    LocalDateTime getConfirmedOrder();
    String getTransactionOrderDate();

    String getFirstName();
    String getLastName();
    String getEmail();
    String getDocumentNumber();
    String getPhone();
    String getBirthDate();
    String getAnonimous();
    String getInkaClub();
    String getNotificationToken();
    String getUserId();
    String getNewUserId();

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

    String getServiceTypeShortCode();
    String getServiceTypeCode();
    String getServiceTypeName();
    String getServiceType();
    String getServiceEnabled();

    String getPaymentType();
    Integer getCardProviderId();
    String getCardProvider();
    String getCardProviderCode();
    String getBin();
    String getCoupon();
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
    String getAddressReceiver();
    String getSource();
    String getOrderNotes();
    String getSourceCompanyName();

    String getDistrictCode();
    Long getZoneId();
    Integer getDaysPickup();
}
