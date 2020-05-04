package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderTrackerResponseCanonical {

    private String statusCode;
    private String statusDescription;
    private String statusDetail;
}
