package com.inretailpharma.digital.ordermanager.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "service_local_order")
public class ServiceLocalOrder {

    @EmbeddedId
    private ServiceLocalOrderIdentity serviceLocalOrderIdentity;

    @Column(name="lead_time")
    private Integer leadTime;

    @Column(name="days_to_pickup")
    private Integer daysToPickup;

    @Column(name="start_hour")
    private LocalTime startHour;

    @Column(name="end_hour")
    private LocalTime endHour;

    private Integer attempt;

    private Integer reprogrammed;

}
