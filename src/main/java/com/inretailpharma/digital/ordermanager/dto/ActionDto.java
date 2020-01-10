package com.inretailpharma.digital.ordermanager.dto;

import com.inretailpharma.digital.ordermanager.util.Constant;
import lombok.Data;

@Data
public class ActionDto {

    private Constant.ActionOrder action;
    private OrderStatusDto orderStatusDto;
    private String externalBillingId;
    private String trackerId;
}
