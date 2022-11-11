package com.inretailpharma.digital.deliverymanager.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface IOrderFulfillment {

    Long getOrderId();
    Long getEcommerceId();
    Long getTrackerId();
    Long getExternalId();
    Long getPurchaseId();
    String getExternalChannelId();


    BigDecimal getTotalCost();
    BigDecimal getSubTotalCost(); // gross price
    BigDecimal getTotalCostNoDiscount();
    BigDecimal getDeliveryCost();
    BigDecimal getDiscountApplied();

    LocalDateTime getCreatedOrder();
    LocalDateTime getScheduledTime();
    LocalDateTime getConfirmedOrder();
    LocalDateTime getCancelledOrder();

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
    String getReferralCode();
    String getReferralMessage();

    String getCenterCode();
    String getCenterName();
    String getCompanyCode();
    String getCompanyName();

    Integer getLeadTime();
    LocalTime getStartHour();
    LocalTime getEndHour();

    String getStatusCode();
    String getStatusName();
    String getStatusDetail();
    String getCancellationCode();
    String getCancellationDescription();

    String getStockType();

    String getServiceTypeShortCode();
    String getServiceTypeCode();
    String getServiceTypeName();
    String getServiceType();
    String getServiceEnabled();
    String getServiceChannel();
    String getClassImplement();
    boolean getSendNewFlow();
    boolean getSendNotificationByChannel();

    String getPaymentType();
    Integer getCardProviderId();
    String getCardProvider();
    String getCardProviderCode();
    String getBin();
    String getCoupon();
    BigDecimal getPaidAmount();
    BigDecimal getChangeAmount();
    LocalDateTime getTransactionDateVisanet();

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

    // Person to pickup order when the order is ret

    String getPickupUserId();
    String getPickupFullName();
    String getPickupEmail();
    String getPickupDocumentType();
    String getPickupDocumentNumber();
    String getPickupPhone();

    Boolean getPartial();
    String getPaymentTransactionId();

    /**
     * Fecha: 15/04/2021
     * autor: Equipo Growth
     * Campos referentes a 3 precios
     */
    BigDecimal getSubTotalWithNoSpecificPaymentMethod();
    BigDecimal getTotalWithNoSpecificPaymentMethod();
    BigDecimal getTotalWithPaymentMethod();
    String getPaymentMethodCardType();
    BigDecimal getDiscountAppliedNoDP();
    /** ********************* **/


    /**
     * Fecha: 04-05-21
     * autor: Carlos CAlla
     * Campos referentes a los estdos de liquidation
     */
    String getLiquidationStatus();
    String getLiquidationStatusDetail();

    boolean getMixedOrder();

    Long getGroupId();

    boolean getVoucher();
    boolean getExternalRouting();
}
