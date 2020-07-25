package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiptCanonical {
    private String type;
    private String companyName;
    private String companyAddress;
    private String companyId;
}
