package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class UserDto {

    @NotBlank
    @Pattern(regexp = "^[0-9]*$")
    private String dni;
    @NotNull
    @Pattern(regexp = "^[YN]$")
    private String isInkaClub;
    @NotBlank
    private String phone;
    @NotBlank
    private String email;
    @NotBlank
    private String name;
    @NotBlank
    private String lastName;
    @NotNull
    @Pattern(regexp = "^[YN]$")
    private String isAnonymous;

    @Override
    public String toString() {
        return "UserDto{" +
                "dni='" + dni + '\'' +
                ", isInkaClub=" + isInkaClub +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", lastName='" + lastName + '\'' +
                ", isAnonymous=" + isAnonymous +
                '}';
    }
}