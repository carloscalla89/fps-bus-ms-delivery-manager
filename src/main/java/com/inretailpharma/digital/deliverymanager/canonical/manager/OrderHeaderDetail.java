package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderHeaderDetail {
    private Long orderId;
    private Long ecommerceId;
    private Long ecommerceIdCall;
    private String companyCode;
    private String serviceChannel;
    private String source;
    private String orderType;
    private String serviceTypeShortCode;
    private String scheduledTime;
    private String statusName;
    private String localCode;
    private String clientName;
    private String documentNumber;
    private String phone;
    private String email;
    private String addressClient;
    private String coordinates;
    private String reference;
    private String ruc;
    private String companyName;
    private String serviceType;
    private String purcharseId;
    private String observation;
    private String cancelReason;
    private String zoneId;
    private String stockType;
}
