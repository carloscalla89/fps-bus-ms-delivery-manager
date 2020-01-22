package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Embeddable
public class ServiceLocalOrderIdentity implements Serializable {

    public ServiceLocalOrderIdentity() {

    }

    public ServiceLocalOrderIdentity(ServiceType serviceType, Local local, OrderFulfillment orderFulfillment, OrderStatus orderStatus) {
        this.serviceType = serviceType;
        this.local = local;
        this.orderFulfillment = orderFulfillment;
        this.orderStatus = orderStatus;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="service_type_code",referencedColumnName = "code")
    private ServiceType serviceType;

    /*
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="local_code",referencedColumnName = "code")
    private Local local;

     */

    @MapsId("localIdentity")
    @JoinColumns({
            @JoinColumn(name="code",referencedColumnName="code"),
            @JoinColumn(name="company_code",referencedColumnName="company_code"),
    })
    @OneToOne(cascade = CascadeType.ALL)
    private Local local;


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="order_fulfillment_id",referencedColumnName = "id")
    private OrderFulfillment orderFulfillment;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_status_code",referencedColumnName = "code")
    private OrderStatus orderStatus;
}
