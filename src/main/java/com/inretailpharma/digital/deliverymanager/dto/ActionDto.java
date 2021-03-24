package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class ActionDto {

    private String action;
    private String companyCode;
    private OrderStatusDto orderStatusDto;
    private String externalBillingId;
    private String orderCancelCode;
    private String orderCancelReason;
    private String orderCancelClientReason;
    private String orderCancelAppType;
    private String orderCancelObservation;
    private String updatedBy;
    private String origin;
    private List<InvoicedOrderDto> invoicedOrderList;
}
