package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ClientCanonical {

    private String fullName;
    private String email;
    private String documentNumber;
    private String phone;
    private String birthDate;
    private Integer hasInkaClub;
    private Integer anonimous;

    // For object of inkatrackerlite
    private String firstName;
    private String lastName;

    private String userId;
    private String notificationToken;
    private String newUserId;

}
