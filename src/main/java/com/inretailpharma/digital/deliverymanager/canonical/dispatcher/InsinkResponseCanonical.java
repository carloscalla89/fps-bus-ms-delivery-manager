package com.inretailpharma.digital.deliverymanager.canonical.dispatcher;

import lombok.Data;

import java.io.Serializable;

@Data
public class InsinkResponseCanonical implements Serializable {

    private String inkaventaId;
    private String errorCode;
    private String successCode;
    private String message;
    private String messageDetail;
    private boolean successfullyInvoke;
    private String callSource;
    private boolean retryInkaventaDelivery;
}
