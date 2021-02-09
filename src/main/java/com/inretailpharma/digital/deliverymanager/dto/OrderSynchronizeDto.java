package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

@Data
public class OrderSynchronizeDto {

    private Long ecommerceId;
    private String actionToOrder;
    private String origin;
}
