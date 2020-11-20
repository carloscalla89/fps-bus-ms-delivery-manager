package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Embeddable
public class ServiceLocalOrderIdentity implements Serializable {

    public ServiceLocalOrderIdentity() {

    }

    public ServiceLocalOrderIdentity(ServiceType serviceType, OrderFulfillment orderFulfillment,
                                     OrderStatus orderStatus) {
        this.serviceType = serviceType;
        this.orderFulfillment = orderFulfillment;
        this.orderStatus = orderStatus;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="service_type_code",referencedColumnName = "code")
    private ServiceType serviceType;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="order_fulfillment_id",referencedColumnName = "id")
    private OrderFulfillment orderFulfillment;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "order_status_code",referencedColumnName = "code")
    private OrderStatus orderStatus;
}
