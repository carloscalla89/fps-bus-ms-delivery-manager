package com.inretailpharma.digital.deliverymanager.dto.LiquidationDto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatusOnlineDto {

    private String action;
    private BigDecimal changeAmount;

    public StatusOnlineDto() {}

    public StatusOnlineDto(String action) {
        this.action = action;
        this.changeAmount = new BigDecimal(0.0);
    }
}
