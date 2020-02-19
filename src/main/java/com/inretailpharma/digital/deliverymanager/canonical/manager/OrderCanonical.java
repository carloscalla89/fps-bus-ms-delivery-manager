package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCanonical {

    // Canonical IDs
    private Long id;
    private Long ecommerceId;
    private Long trackerId;
    private Long externalId;
    private Long bridgePurchaseId;


    // Canonical cost
    private BigDecimal deliveryCost;
    private BigDecimal discountApplied;
    private BigDecimal subTotalCost;
    private BigDecimal totalAmount;

    // Canonical local and company
    private String localCode;
    private String local;
    private String company;

    // canonical client
    private ClientCanonical client;

    // canonical status
    private OrderStatusCanonical  orderStatus;

    // Canonical receipt
    private ReceiptCanonical receipt;

    // Canonical PaymentMethod;
    private PaymentMethodCanonical paymentMethod;

    // Canonical Address delivery
    private AddressCanonical address;

    // Canonical items
    private List<OrderItemCanonical> orderItems;

    // Canonical serviceType
    private OrderDetailCanonical orderDetail;

    // attempts
    private Integer attempt;
    private Integer attemptTracker;
}
