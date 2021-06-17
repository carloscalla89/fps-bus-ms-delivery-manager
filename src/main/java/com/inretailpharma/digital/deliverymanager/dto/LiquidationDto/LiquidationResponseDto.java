package com.inretailpharma.digital.deliverymanager.dto.LiquidationDto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LiquidationResponseDto {

    private String statusCode;
    private String statusName;
    private String statusDetail;

}
