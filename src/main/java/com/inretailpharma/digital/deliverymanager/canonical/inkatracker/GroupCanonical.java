package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupCanonical implements Serializable {

    private Integer position;
    private Long orderId;
    private EtaCanonical eta;
    private Long timeRemaining;
}
