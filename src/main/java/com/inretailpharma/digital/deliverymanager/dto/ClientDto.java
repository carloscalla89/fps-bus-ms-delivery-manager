package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ClientDto {

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String documentNumber;
    private String phone;
    private String birthDate;
    private Integer hasInkaClub;
    private Integer anonimous;
}
