package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Embeddable
@Table(name = "address_fulfillment")
public class Address {

    @Column(table = "address_fulfillment", name="name")
    private String name;

    @Column(table = "address_fulfillment", name="district")
    private String district;

    @Column(table = "address_fulfillment", name="street")
    private String street;

    @Column(table = "address_fulfillment", name="number")
    private String number;

    @Column(table = "address_fulfillment", name="province")
    private String province;

    @Column(table = "address_fulfillment", name="apartment")
    private String apartment;

    @Column(table = "address_fulfillment", name="city")
    private String city;

    @Column(table = "address_fulfillment", name="department")
    private String department;

    @Column(table = "address_fulfillment", name="country")
    private String country;

    @Column(table = "address_fulfillment", name="latitude")
    private BigDecimal latitude;

    @Column(table = "address_fulfillment", name="longitude")
    private BigDecimal longitude;

    @Column(columnDefinition = "TEXT", table = "address_fulfillment", name="notes")
    private String notes;

    @Column(table = "address_fulfillment", name="postal_code")
    private String postalCode;

    @Column(table = "address_fulfillment", name="receiver")
    private String receiver;
}
