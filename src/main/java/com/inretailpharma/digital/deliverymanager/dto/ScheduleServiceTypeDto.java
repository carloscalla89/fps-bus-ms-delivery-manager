package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ScheduleServiceTypeDto {

    private String startHourZone;

    private String endHourZone;

    private Integer deliveryLeadTime;

    private Integer daysToPickup;

    private String startHourPickup;

    private String endHourPickup;


}
