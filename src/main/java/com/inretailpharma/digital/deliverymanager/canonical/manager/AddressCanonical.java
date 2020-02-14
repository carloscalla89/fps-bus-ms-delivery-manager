package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressCanonical {

    private String department;
    private String province;
    private String district;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String name;
    private String zipCode;
}
