package com.inretailpharma.digital.ordermanager.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class ServiceLocalOrderIdentity implements Serializable {

    @Column(name="service_type_code")
    private String serviceTypeCode;

    @Column(name="local_code")
    private String localCode;

    @Column(name="order_fulfillment_id")
    private Long orderTrackerId;

    @Column(name="order_status_code")
    private String orderStatusCode;
}
