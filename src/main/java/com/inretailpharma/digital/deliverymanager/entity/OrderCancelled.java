package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity
@Table(name = "order_fulfillment_cancelled")
public class OrderCancelled {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="order_fulfillment_id",referencedColumnName = "id")
    private OrderFulfillment orderFulfillment;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="code_cancellation",referencedColumnName = "code")
    private CancellationCodeReason cancellationCodeReason;

    private String observation;

}
