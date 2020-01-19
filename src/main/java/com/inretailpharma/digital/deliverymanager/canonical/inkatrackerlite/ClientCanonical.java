package com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ClientCanonical {

    private String userId;
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    private String email;
    @Pattern(regexp = "[A-Za-z0-9]+", message = "Only alphanumeric characters")
    @Length(max = 15)
    private String dni;
    @Pattern(regexp = "\\d+", message = "Only numbers")
    private String phone;
    private Long birthDate;
    @Pattern(regexp = "^(Y|N)$", message = "Y or N")
    private String hasInkaClub;
    @Pattern(regexp = "^(Y|N)$", message = "Y or N")
    private String isAnonymous;
    private String notificationToken;

}
