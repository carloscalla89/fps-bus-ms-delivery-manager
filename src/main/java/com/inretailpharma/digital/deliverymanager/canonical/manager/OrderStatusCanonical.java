package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusCanonical implements Serializable {

    private String code;
    private String name;
    private String detail;
    private String cancellationCode;
    private String cancellationDescription;
    private String cancellationObservation;
    private String statusDate;
    @JsonIgnore
    private String firstStatusName;
    private boolean successful;

}
