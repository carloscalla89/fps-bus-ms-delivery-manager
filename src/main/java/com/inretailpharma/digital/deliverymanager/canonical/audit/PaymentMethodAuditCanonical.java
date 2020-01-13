package com.inretailpharma.digital.deliverymanager.canonical.audit;

import lombok.Data;

@Data
public class PaymentMethodAuditCanonical {

    private String type;
    private String providerCard;

}
