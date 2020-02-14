package com.inretailpharma.digital.deliverymanager.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class AddressDto {
    private String name;
    private String street;
    private String number;
    private String apartment;
    private String country;
    private String city;
    private String district;
    private String notes;
    private Double latitude;
    private Double longitude;
}
