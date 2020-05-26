package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class DrugstoreCanonical {

    private Long id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer radius;
}
