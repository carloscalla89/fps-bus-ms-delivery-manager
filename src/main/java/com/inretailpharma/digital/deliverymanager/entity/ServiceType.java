package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "service_type")
public class ServiceType {
    @Id
    private String code;
    private String name;
    private String type;
    @Column(name="source_channel")
    private String sourceChannel;
    private String description;
    private String enabled;

}