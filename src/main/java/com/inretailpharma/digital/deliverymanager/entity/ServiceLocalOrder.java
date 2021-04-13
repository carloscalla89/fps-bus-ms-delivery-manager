package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
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

    @Column(name="center_code")
    private String centerCode;

    @Column(name="company_code")
    private String companyCode;

    @Column(name="district_code_billing")
    private String districtCodeBilling;

    @Column(name="zone_id_billing")
    private Long zoneIdBilling;


    // Entities to save the person pickup
    @Column(name="pickup_user_id")
    private String pickupUserId;

    @Column(name="pickup_full_name")
    private String pickupFullName;

    @Column(name="pickup_email")
    private String pickupEmail;

    @Column(name="pickup_document_type")
    private String pickupDocumentType;

    @Column(name="pickup_document_number")
    private String pickupDocumentNumber;

    @Column(name="pickup_phone")
    private String pickupPhone;

    @Column(name="date_created")
    private LocalDateTime dateCreated;

    @Column(name="date_last_updated")
    private LocalDateTime dateLastUpdated;

    @Column(name="date_cancelled")
    private LocalDateTime dateCancelled;


}
