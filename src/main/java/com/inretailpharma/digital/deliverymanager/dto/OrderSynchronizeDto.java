package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderSynchronizeDto {

    private Long ecommerceId;
    private String origin;
    private List<HistorySynchronizedDto> history;

}
