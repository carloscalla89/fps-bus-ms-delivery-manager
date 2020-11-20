package com.inretailpharma.digital.deliverymanager.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.PersonToPickupDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OrderDto {

    private String source;

    @NotNull
    private String localCode;
    private String companyCode;
    private String serviceTypeCode;

    @NotNull
    private Long ecommercePurchaseId;
    private Long trackerId;
    private Long externalPurchaseId;
    private Integer purchaseNumber;

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

    private List<OrderItemDto> orderItem;
    private List<OrderItemEditedDto> itemsRetired;

    private ScheduleServiceTypeDto schedules;

    private Constant.ActionOrder action;

    private Boolean programmed;

    private String notes;
    private String sourceCompanyName;

    // campos que son necesarios para enviar al insink
    private String districtCodeBilling;
    private Long zoneIdBilling;

    private PersonToPickupDto personToPickup;
}
