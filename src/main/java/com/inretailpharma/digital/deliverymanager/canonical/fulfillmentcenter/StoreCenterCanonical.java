package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreCenterCanonical {

    private Long legacyId;
    private String inkaVentaId;
    private String localCode;
    private String companyCode;
    private String name;
    private String description;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String localType;
    private List<ServiceOffered> services;
    private boolean externalRoutingEnabled;

    public StoreCenterCanonical() {

    }

    public StoreCenterCanonical(String localCode) {
        this.localCode = localCode;
    }

    public StoreCenterCanonical(String localCode, String companyCode) {
        this.localCode = localCode;
        this.companyCode = companyCode;
    }

}
