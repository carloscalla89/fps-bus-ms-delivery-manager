package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Column;
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
    @Column(name="app_type")
    private String appType;
    private String reason;
    @Column(name="client_reason")
    private String clientReason;
    private Integer enabled;
}
