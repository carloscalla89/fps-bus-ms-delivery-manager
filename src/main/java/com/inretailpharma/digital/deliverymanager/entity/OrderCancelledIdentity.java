package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Embeddable
public class OrderCancelledIdentity implements Serializable {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="order_fulfillment_id",referencedColumnName = "id")
    private OrderFulfillment orderFulfillment;

}
