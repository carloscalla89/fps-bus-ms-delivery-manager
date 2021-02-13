package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuditHistoryDto {

    private Long ecommerceId;
    private String source;
    private String timeFromUi;
    private String updatedBy;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String statusCode;
    private String statusName;

}
