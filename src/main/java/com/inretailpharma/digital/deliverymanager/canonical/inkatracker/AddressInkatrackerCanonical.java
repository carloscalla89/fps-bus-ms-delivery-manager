package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class AddressInkatrackerCanonical {

    private Long id;
    private String name;
    private String street;
    private String number;
    private String apartment;
    private String country;
    private String city;
    private String district;
    private String notes;
    private Double latitude;
    private Double longitude;
    private Integer zoneEta;
}
