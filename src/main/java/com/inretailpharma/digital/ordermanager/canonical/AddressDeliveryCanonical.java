package com.inretailpharma.digital.ordermanager.canonical;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressDeliveryCanonical {

    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
}
