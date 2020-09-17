package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ScheduleServiceTypeDto {

    private String createdOrder;

    private String scheduledTime;

    private String confirmedOrder;

    private String cancelledOrder;

    private String confirmedInsinkOrder;

    private String transactionVisaOrder;

    private String startHour;

    private String endHour;

    private Integer leadTime;

    private Integer daysToPickup;

}
