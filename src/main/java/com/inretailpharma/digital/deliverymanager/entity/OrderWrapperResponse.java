package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class OrderWrapperResponse {

    private Long fulfillmentId;
    private Long trackerId;
    private String orderStatusCode;
    private String orderStatusName;
    private String orderStatusDetail;

    private String serviceCode;
    private String serviceShortCode;
    private String serviceType;
    private String serviceName;
    private String serviceSourcechannel;
    private String serviceClassImplement;
    private boolean serviceSendNewFlowEnabled;
    private boolean serviceSendNotificationEnabled;

    private String serviceEnabled;

    private String cancellationCode;
    private String cancellationDescription;
    private LocalTime startHour;
    private LocalTime endHour;
    private int daysToPickup;
    private int leadTime;
    private Integer attemptBilling;
    private Integer attemptTracker;
    private String localCode;
    private String localName;
    private String companyCode;
    private String companyName;
    private String receiptName;
    private String paymentMethodName;

    private Long localId;
    private String localDescription;
    private String localAddress;
    private BigDecimal localLatitude;
    private BigDecimal localLongitude;
    private Integer localRadius;

    // liquidation status and detail
    private boolean liquidationEnabled;
    private String liquidationStatus;
    private String liquidationStatusDetail;


}
