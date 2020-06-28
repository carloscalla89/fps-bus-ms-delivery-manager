package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PartialOrderDto {

    private BigDecimal productsTotalCost;
    private BigDecimal productsSubTotalCost;
    private BigDecimal deliveryCost;
    private BigDecimal discount;
    private BigDecimal partialChange;
    private List<OrderItemDto> items;
}
