package com.inretailpharma.digital.deliverymanager.dto.LiquidationDto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatusDto {
    //@NotBlank(message = "status.code may not be blank")
    private String code;
  //  @NotBlank(message = "status.name may not be blank")
    private String name;
    private String detail;
    private String cancellationCode;
    private String cancellationDescription;
    private String origin;
    private BigDecimal totalCost;
    private BigDecimal changeAmount;

    public StatusDto() {}

    public StatusDto(String code) {
        this.code = code;
    }
}
