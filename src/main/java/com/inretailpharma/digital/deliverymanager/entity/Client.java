package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "client_fulfillment")
public class Client extends OrderEntity<Long> {

    @Column(name="first_name")
    private String firstName;

    @Column(name="last_name")
    private String lastName;

    private String email;

    @Column(name="document_number")
    private String documentNumber;

    private String phone;

    @Column(name="birth_date")
    private LocalDate birthDate;

    private Integer inkaclub;

    private Integer anonimous;

}
