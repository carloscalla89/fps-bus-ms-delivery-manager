package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ScheduleServiceTypeDto {

    private String createdOrder;

    private String scheduledTime;

    private String confirmedOrder;

    private String startHour;

    private String endHour;

    private Integer leadTime;

}
