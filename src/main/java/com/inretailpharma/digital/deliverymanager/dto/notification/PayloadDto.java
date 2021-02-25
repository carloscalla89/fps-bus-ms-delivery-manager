package com.inretailpharma.digital.deliverymanager.dto.notification;

import lombok.Data;

@Data
public class PayloadDto {

    private String clientName;
    private String expiredDate;
    private String confirmedDate;
    private String address;
    private String localCode;

}
