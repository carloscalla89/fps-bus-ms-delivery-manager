package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class CancellationCanonical {

    private String code;
    private String type;
    private String appType;
    private String description;
}
