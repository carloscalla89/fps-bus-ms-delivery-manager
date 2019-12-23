package com.inretailpharma.digital.ordermanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "order_process_status")
public class ServiceLocalOrder {

    @EmbeddedId
    private ServiceLocalOrderIdentity serviceLocalOrderIdentity;

    /*
    @ManyToOne
    @JoinColumn(name="service_type_code",insertable = false, updatable = false)
    private ServiceType serviceType;



    @ManyToOne
    @JoinColumn(name="order_fulfillment_id",insertable = false, updatable = false)
    private OrderFulfillment orderFulfillment;

    @ManyToOne
    @JoinColumn(name = "order_status_code",insertable = false, updatable = false)
    private OrderStatus orderStatus;


     */

    /*
    @OneToOne
    @JoinColumn(name="local_code",insertable = false, updatable = false)
    private Local local;


     */

    /*
    @MapsId("drugstoreDeliveryTypeFrameIdentity")
    @OneToOne(cascade = CascadeType.ALL)
    //@OneToOne(mappedBy = "serviceLocalOrder", cascade = CascadeType.ALL)
    @JoinColumn(name="service_type_code", referencedColumnName = "code",insertable = false, updatable = false)
    private ServiceType serviceType;

    @MapsId("drugstoreDeliveryTypeFrameIdentity")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="local_code", referencedColumnName = "code",insertable = false, updatable = false)
    private Local local;

    @MapsId("drugstoreDeliveryTypeFrameIdentity")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="order_fulfillment_id", referencedColumnName = "id",insertable = false, updatable = false)
    private OrderFulfillment orderFulfillment;

    @MapsId("drugstoreDeliveryTypeFrameIdentity")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_status_code", referencedColumnName = "code",insertable = false, updatable = false)
    private OrderStatus orderStatus;


     */

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

    private Integer reprogrammed;

}
