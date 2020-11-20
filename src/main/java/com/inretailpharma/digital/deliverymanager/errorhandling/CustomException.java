package com.inretailpharma.digital.deliverymanager.errorhandling;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CustomException extends Exception {

    public CustomException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    private Integer statusCode;



}
