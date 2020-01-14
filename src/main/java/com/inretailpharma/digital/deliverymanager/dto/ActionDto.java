package com.inretailpharma.digital.deliverymanager.dto;

import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.Data;

@Data
public class ActionDto {

    private Constant.ActionOrder action;
    private OrderStatusDto orderStatusDto;
    private String externalBillingId;
    private String trackerId;
    private String orderCancelCode;
    private String orderCancelObservation;
}
