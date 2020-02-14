package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

@Data
public class ReceiptCanonical {

    private String type;
    private String ruc;
    private String companyName;
    private String address;
}
