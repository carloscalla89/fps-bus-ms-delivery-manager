package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.inretailpharma.digital.deliverymanager.util.DateUtils;

@Data
public class OrderCanonical {

    public OrderCanonical() {

    }

    public OrderCanonical(String code, String name) {
        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
    }

    public OrderCanonical(Long ecommerceId, String code, String name) {
        this.ecommerceId = ecommerceId;

        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
    }
    
    public OrderCanonical(Long ecommerceId, String code, String name, String detail) {
        this.ecommerceId = ecommerceId;

        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
        this.orderStatus.setDetail(detail);
        this.orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
    }

    // Canonical IDs
    private Long id;
    private Long ecommerceId;
    private Long trackerId;
    private Long externalId;
    private Long bridgePurchaseId;
    private String motorizedId;

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
    
    // Canonical shelf
    private List<ShelfCanonical> shelfList;
    private String payBackEnvelope;

    // versión anterior
    private Integer attempt;
    private Integer attemptTracker;

}
