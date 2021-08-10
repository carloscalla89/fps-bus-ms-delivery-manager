package com.inretailpharma.digital.deliverymanager.canonical.ordertracker;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupCanonical implements Serializable{

	private Integer position;
    private Long orderId;
    private EtaCanonical eta;
    private Long timeRemaining;
    private PickUpDetailsCanonical pickUpDetails;
    private OrderCanonical order;

    @Override
    public String toString() {
        return "GroupCanonical{" +
                "position=" + position +
                ", orderId=" + orderId +
                '}';
    }
}
