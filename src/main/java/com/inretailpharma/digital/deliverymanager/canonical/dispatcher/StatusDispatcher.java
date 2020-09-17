package com.inretailpharma.digital.deliverymanager.canonical.dispatcher;

import lombok.Data;

@Data
public class StatusDispatcher {

    private String code;
    private boolean successProcess;
    private String description;
    private String detail;
}
