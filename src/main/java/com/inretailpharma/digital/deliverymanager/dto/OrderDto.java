package com.inretailpharma.digital.deliverymanager.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OrderDto {

    private String source;
    private String localCode;
    private String companyCode;
    private String serviceTypeCode;

    private Long ecommercePurchaseId;
    private Long trackerId;
    private Long externalPurchaseId;
    private Long bridgePurchaseId;

    private BigDecimal deliveryCost;
    private BigDecimal discountApplied;
    private BigDecimal subTotalCost;
    private BigDecimal totalCost;

    private OrderStatusDto orderStatusDto;

    private ClientDto client;

    private AddressDto address;

    private ShipperDto shipper;

    private PaymentMethodDto payment;

    private ReceiptTypeDto receipt;

    private List<OrderDetailDto> orderItem;

    private ScheduleServiceTypeDto schedules;

    private Constant.ActionOrder action;

    private Boolean programmed;

    // campos anteriores
    private String createdOrder;

    private String scheduledTime;

}
