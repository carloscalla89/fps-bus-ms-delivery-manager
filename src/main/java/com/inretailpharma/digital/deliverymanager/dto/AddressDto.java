package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AddressDto {
    private String name;
    private String street;
    private String number;
    private String apartment;
    private String city;
    private String district;
    private String province;
    private String department;
    private String country;
    private String notes;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String receiver;
}
