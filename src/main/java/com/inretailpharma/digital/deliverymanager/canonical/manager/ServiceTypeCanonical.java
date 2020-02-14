package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class ServiceTypeCanonical {

    private String code;
    private String name;
    private String type;
    private Integer leadTime;
    private Integer daysToPickup;
    private String startHour;
    private String endHour;
}
