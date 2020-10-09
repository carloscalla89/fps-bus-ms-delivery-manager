package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class PersonToPickupDto {

    private String userId;
    private String fullName;
    private String email;
    private String identityDocumentType;
    private String identityDocumentNumber;
    private String phone;

}
