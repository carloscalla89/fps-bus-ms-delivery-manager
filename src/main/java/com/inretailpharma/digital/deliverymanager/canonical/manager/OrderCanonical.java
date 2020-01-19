package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.inretailpharma.digital.deliverymanager.canonical.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderCanonical {

    private Long id;
    private Long ecommerceId;
    private Long trackerId;
    private Long externalId;

    //
    private String clientFullName;
    private String documentNumber;
    private String leadTime;
    private BigDecimal totalAmount;

    private Integer attempt;
    private Integer attemptTracker;

    // Canonical local and company
    private String localCode;
    private String local;
    private String company;

    // canonical status
    private OrderStatusCanonical  orderStatus;

    // Canonical serviceType
    private ServiceTypeCanonical serviceType;


    // Canonical receipt
    private ReceiptCanonical receipt;

    // Canonical PaymentMethod;
    private PaymentMethodCanonical paymentMethod;

    // Canonical Address delivery
    private AddressDeliveryCanonical addressDelivery;


}
