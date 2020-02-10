package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name="cancellation_code_reason")
public class CancellationCodeReason {

    @Id
    private String code;
    private String type;
    private String reason;
}
