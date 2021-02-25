package com.inretailpharma.digital.deliverymanager.dto.notification;

import lombok.Data;

@Data
public class MessageDto {

    private String brand;
    private String orderStatus;
    private String channel;
    private String deliveryTypeCode;
    private String orderId;
    private String localTypeCode;
    private String phoneNumber;
    private PayloadDto payload;

}
