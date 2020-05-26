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
    // For object of inkatrackerlite
    private String nameAddress;
    private String street;
    private String number;
    private String city;
    private String apartment;
    private String notes;
}
