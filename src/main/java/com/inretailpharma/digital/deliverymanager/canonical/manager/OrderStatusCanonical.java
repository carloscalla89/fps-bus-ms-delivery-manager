package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatusCanonical implements Serializable {

    private String code;
    private String name;
    private String detail;
    private String cancellationCode;
    private String cancellationDescription;
    private String cancellationObservation;
    private String statusDate;
    @JsonIgnore
    private String firstStatusName;
    private boolean successful;
    
    private BigDecimal latitude;
    private BigDecimal longitude;

    public OrderStatusCanonical() {
    }

    public OrderStatusCanonical(OrderStatusCanonical orderStatusCanonical) {
        this.code = orderStatusCanonical.getCode();
        this.name = orderStatusCanonical.getName();
        this.detail = orderStatusCanonical.getDetail();
        this.cancellationCode = orderStatusCanonical.getCancellationCode();
        this.cancellationDescription = orderStatusCanonical.getCancellationDescription();
        this.cancellationObservation = orderStatusCanonical.getCancellationObservation();
        this.statusDate = orderStatusCanonical.getStatusDate();
        this.firstStatusName = orderStatusCanonical.getFirstStatusName();
        this.successful = orderStatusCanonical.isSuccessful();
        this.latitude = orderStatusCanonical.getLatitude();
        this.longitude = orderStatusCanonical.getLongitude();
    }
}
