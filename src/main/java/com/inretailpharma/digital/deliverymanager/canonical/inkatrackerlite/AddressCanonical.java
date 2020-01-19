package com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class AddressCanonical {

    private Long id;
    @NotNull
    private String name;
    @NotNull
    private String street;
    private String number;
    private String apartment;
    private String country;
    private String city;
    private String district;
    private String notes;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;

}
