package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {

    private String street;
    private String number;
    private String apartment;
    private String country;
    private String city;
    private String district;
    private String notes;
    private Double latitude;
    private Double longitude;
    private String receiverName;

    @Override
    public String toString() {
        return "AddressDto{" +
                "street='" + street + '\'' +
                ", number='" + number + '\'' +
                ", apartment='" + apartment + '\'' +
                ", country='" + country + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", notes='" + notes + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", receiverName='" + receiverName + '\'' +
                '}';
    }
}
