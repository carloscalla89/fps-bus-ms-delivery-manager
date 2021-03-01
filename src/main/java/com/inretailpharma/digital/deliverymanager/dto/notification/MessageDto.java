package com.inretailpharma.digital.deliverymanager.dto.notification;

import lombok.Data;

@Data
public class MessageDto {

    private String brand;
    private String orderStatus;
    private String channel;
    private String deliveryTypeCode;
    private Long orderId;
    private String localType;
    private String phoneNumber;
    private PayloadDto payload;

}
