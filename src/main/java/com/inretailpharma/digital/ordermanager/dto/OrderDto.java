package com.inretailpharma.digital.ordermanager.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class OrderDto {

    private Long id;
    private String source;
    private String serviceTypeCode;
    private String localCode;
    private Long ecommercePurchaseId;
    private Long bridgePurchaseId;
    private OrderStatusDto orderStatusDto;
    private BigDecimal deliveryCost;
    private BigDecimal totalCost;

    private ClientDto client;

    private AddressDto address;

    private String createdOrder;
    private String scheduledTime;

    private String notes;

    private ShipperDto shipper;

    private PaymentMethodDto payment;

    private ReceiptTypeDto receipt;

    private List<OrderDetailDto> orderItem;




}
