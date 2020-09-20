package com.inretailpharma.digital.deliverymanager.errorhandling;

import lombok.Data;

import java.util.List;

@Data
public class ServerResponseError {

    private String path;
    private int statusCode;
    private String errorType;
    private List<String> errors;

}
