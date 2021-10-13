package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "client_fulfillment")
public class Client  implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name="notification_token")
    private String notificationToken;

    @Column(name="user_id")
    private String userId;

    @Column(name="new_user_id")
    private String newUserId;

    @Column(name="referral_code")
    private String referralCode;

    @Column(name="referral_msg")
    private String referralMessage;

}
