package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Data
@Entity
@SuppressWarnings("all")
@Table(name = "application_parameter")
public class ApplicationParameter implements Serializable {

    @Id
    private String code;
    private String description;
    private String value;
}
