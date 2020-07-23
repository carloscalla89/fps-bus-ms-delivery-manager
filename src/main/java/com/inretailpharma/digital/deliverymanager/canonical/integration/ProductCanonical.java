package com.inretailpharma.digital.deliverymanager.canonical.integration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductCanonical {

    private String id;
    private String name;
    private Integer presentationId;
    private String presentation;
    private String eanCode;
    private Integer quantityUnits;
    private String sapCode;
    private String familyType;
    private double fractionatedPrice;

}
