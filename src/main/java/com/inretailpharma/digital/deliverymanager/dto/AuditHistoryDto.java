package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuditHistoryDto {

    private Long ecommerceId;
    private String source;
    private String target;
    private String timeFromUi;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String statusCode;
    private String statusName;
    private String statusDetail;
    private String orderNote;
    private String customNote;
    private String localCode;
    private String endScheduleDate;
    private String updatedBy;
    // liquidation status and detail
    private String liquidationStatus;
    private String liquidationStatusDetail;
    // Operations Audit
    private String brand;
    private String deliveryType;
    private String sourceChannel;
    
    private String saleChannel;
    private String saleChannelType;

}
