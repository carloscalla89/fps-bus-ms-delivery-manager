package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancellationCanonical {

    private String code;
    private String type;
    private String appType;
    private String description;
}
