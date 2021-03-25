package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

@Data
public class HistorySynchronizedDto {

    private String action;
    private String actionDate;
    private String orderCancelCode;
    private String updatedBy;
    private String motorizedId;
}
