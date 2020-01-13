package com.inretailpharma.digital.deliverymanager.canonical.audit;

import lombok.Data;

@Data
public class ReceiptAuditCanonical {

    private String type;
    private String ruc;
    private String companyName;
    private String address;
}
