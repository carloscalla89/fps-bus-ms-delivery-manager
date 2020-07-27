package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusCanonical {

    private Integer id;
    private String description;

}
