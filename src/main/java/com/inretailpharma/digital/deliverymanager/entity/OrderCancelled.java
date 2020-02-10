package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity
@Table(name = "order_fulfillment_cancelled")
public class OrderCancelled {

    @EmbeddedId
    private OrderCancelledIdentity orderCancelledIdentity;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="code_cancellation",referencedColumnName = "code")
    private CancellationCodeReason cancellationCodeReason;

    private String observation;

}
