package com.inretailpharma.digital.deliverymanager.canonical.audit;

import lombok.Data;

@Data
public class ServiceLocalCompanyAuditCanonical {

    private String serviceType;
    private String localCode;
    private String local;
    private String company;
}
