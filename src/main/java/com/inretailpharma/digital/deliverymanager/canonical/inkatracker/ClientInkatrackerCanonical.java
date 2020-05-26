package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import lombok.Data;

@Data
public class ClientInkatrackerCanonical {

    private String userId;
    private String joinIdentifierId;
    private String firstName;
    private String lastName;
    private String email;
    private String dni;
    private String phone;
    private Long birthDate;
    private String hasInkaClub;
    private String notificationToken;
    private String isAnonymous;
}
