package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreCenterCanonical {

    private Long legacyId;
    private String localCode;
    private String companyCode;
    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;

    public StoreCenterCanonical() {

    }

    public StoreCenterCanonical(String localCode) {
        this.localCode = localCode;
    }

}
