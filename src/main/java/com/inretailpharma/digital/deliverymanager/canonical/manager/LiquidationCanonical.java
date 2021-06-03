package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LiquidationCanonical {

    /*
      Parameters of liquidations
      date: 05-05-21
      by: carlos calla
     */
    private boolean enabled;
    private String code;
    private String status;
    private String detail;
    /**/
}
