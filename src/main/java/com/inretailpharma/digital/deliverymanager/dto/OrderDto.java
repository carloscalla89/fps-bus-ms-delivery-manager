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

    @NotNull
    private String source;

    private String localCode;
    private String companyCode;
    private String serviceTypeCode;

    @NotNull
    private Long ecommercePurchaseId;
    private String ecommerceIds;
    private Long trackerId;
    private Long externalPurchaseId;
    private Integer purchaseNumber;

    private BigDecimal deliveryCost;
    private BigDecimal discountApplied;
    private BigDecimal subTotalCost; // gross price
    private BigDecimal totalCost;
    private BigDecimal totalCostNoDiscount;
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

    private String externalChannelId;

    /**
     * Fecha: 15/03/2021
     * autor: Carlos Calla
     * para identificar si el stock sale del local original(M=main) o de un local backup(B=backup)
     */
    private String stockType;

    /**
     * Fecha: 15/04/2021
     * autor: Equipo Growth
     * Campos referentes a 3 precios
     */
    private BigDecimal subTotalWithNoSpecificPaymentMethod;
    private BigDecimal totalWithNoSpecificPaymentMethod;
    private BigDecimal totalWithPaymentMethod;
    private String paymentMethodCardType;
    private BigDecimal discountAppliedNoDP;



}
