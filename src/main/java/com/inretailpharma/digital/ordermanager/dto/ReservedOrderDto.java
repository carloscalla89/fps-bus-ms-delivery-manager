package com.inretailpharma.digital.ordermanager.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.ordermanager.util.Constant;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ReservedOrderDto {
    private String externalPurchaseId;
    private String trackerPurchaseId;
    private OrderStatusDto orderStatusDto;

}
