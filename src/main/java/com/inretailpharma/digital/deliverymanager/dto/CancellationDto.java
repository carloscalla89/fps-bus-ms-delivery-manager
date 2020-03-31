package com.inretailpharma.digital.deliverymanager.dto;

import lombok.Data;

@Data
public class CancellationDto {

    private String serviceType;
    private String statusType;
    private String cancellationCode;
    private String observation;

}
