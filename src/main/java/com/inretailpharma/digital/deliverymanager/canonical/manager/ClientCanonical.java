package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ClientCanonical {

    private String firstName;
    private String lastName;
    private String email;
    private String documentNumber;
    private String phone;
    private String birthDate;
    private String hasInkaClub;
    private String anonimous;
}
