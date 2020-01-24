package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

@Data
public class ActionDto {

    private String action;
    private OrderStatusDto orderStatusDto;
    private String externalBillingId;
    private String trackerId;
    private String orderCancelCode;
    private String orderCancelObservation;
}
