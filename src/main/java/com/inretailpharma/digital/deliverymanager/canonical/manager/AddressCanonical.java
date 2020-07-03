package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressCanonical {

    private String name;
    private String department;
    private String province;
    private String district;
    private String country;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String postalCode;
    private String notes;
    private String apartment;
}
