package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

@Data
public class OrderWrapperResponse {

    private Long trackerId;
    private String orderStatusCode;
    private String orderStatusName;
    private String orderStatusDetail;
}
