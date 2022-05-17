package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ActionDto {

    private long ecommerceId;
    private String action;
    private String companyCode;
    private OrderStatusDto orderStatusDto;
    private String orderCancelCode;
    private String orderCancelObservation;
    private String motorizedId;
    private String updatedBy;
    private String origin;
    private String actionDate;
    private List<InvoicedOrderDto> invoicedOrderList;
}
