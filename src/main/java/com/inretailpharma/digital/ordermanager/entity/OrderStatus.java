package com.inretailpharma.digital.ordermanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


@Data
@Embeddable
@Table(name="order_status")
public class OrderStatus implements Serializable {

    @Id
    private String code;
    @Column(table = "order_status", name="type")
    private String type;
    @Column(table = "order_status", name="description")
    private String description;

}
