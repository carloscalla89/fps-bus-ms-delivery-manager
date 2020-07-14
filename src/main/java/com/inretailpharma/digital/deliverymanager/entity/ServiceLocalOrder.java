package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "order_process_status")
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

    @Column(name="status_detail")
    private String statusDetail;

    private Integer attempt;

    @Column(name="attempt_tracker")
    private Integer attemptTracker;

    private Integer reprogrammed;

    @Column(name = "cancellation_code")
    private String cancellationCode;

    @Column(name = "cancellation_app_type")
    private String cancellationAppType;

    @Column(name= "cancellation_observation")
    private String cancellationObservation;

}
