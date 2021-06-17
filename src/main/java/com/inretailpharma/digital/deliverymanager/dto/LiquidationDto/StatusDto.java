package com.inretailpharma.digital.deliverymanager.dto.LiquidationDto;


import lombok.Data;

@Data
public class StatusDto {
    //@NotBlank(message = "status.code may not be blank")
    private String code;
  //  @NotBlank(message = "status.name may not be blank")
    private String name;
    private String detail;
    private String cancellationCode;
    private String cancellationDescription;
}
