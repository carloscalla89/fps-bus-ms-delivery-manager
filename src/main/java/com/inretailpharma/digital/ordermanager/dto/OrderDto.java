package com.inretailpharma.digital.ordermanager.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
public class OrderDto {

    private String id;
    private String source;


    private Long digitalPurchaseId;
    private Long bridgePurchaseId;
    private OrderStatusDto orderStatusDto;
    private BigDecimal delivery_cost;

    private BigDecimal total_cost;

    private ClientDto client;

    private AddressDto address;

    private LocalDateTime createdOrder;

    private LocalDateTime scheduledTime;



    private String notes;


    private ShipperDto shipper;

    private PaymentMethodDto payment;

    private ReceiptTypeDto receipt;

    private List<OrderDetailDto> orderDetailList;




}
