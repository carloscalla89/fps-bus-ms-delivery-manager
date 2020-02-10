package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;

@Data
@MappedSuperclass
@SuppressWarnings("all")
public abstract class OrderEntity<T extends Serializable> extends AuditingEntity {

    /**
     * Unique reference for this entity
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private T id;

}
