package com.inretailpharma.digital.deliverymanager.canonical;

import lombok.Data;

@Data
public class GenericResponseDto {

    private String statusCode;
    private String statusDescription;
    private String statusDetail;
}
