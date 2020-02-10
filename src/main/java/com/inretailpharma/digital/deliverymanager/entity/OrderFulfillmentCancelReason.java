package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Entity
@Table(name="order_fulfillment_cancel_reason")
public class OrderFulfillmentCancelReason {

    private String code;
    private String type;
    private String reason;
}
