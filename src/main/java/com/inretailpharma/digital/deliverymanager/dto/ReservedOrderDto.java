package com.inretailpharma.digital.deliverymanager.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ReservedOrderDto {
    private String externalPurchaseId;
    private String trackerPurchaseId;
    private OrderStatusDto orderStatusDto;

}
