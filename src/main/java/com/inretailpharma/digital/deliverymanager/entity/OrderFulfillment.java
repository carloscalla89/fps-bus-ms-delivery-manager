package com.inretailpharma.digital.deliverymanager.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Clase OrderFulfillment subdominio order tracker
 *
 * @author
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name="order_fulfillment")
@SecondaryTables({
        @SecondaryTable(name="address_fulfillment", pkJoinColumns = @PrimaryKeyJoinColumn(name = "order_fulfillment_id", referencedColumnName = "id")),
        @SecondaryTable(name="payment_method", pkJoinColumns = @PrimaryKeyJoinColumn(name = "order_fulfillment_id", referencedColumnName = "id")),
        @SecondaryTable(name="receipt_type", pkJoinColumns = @PrimaryKeyJoinColumn(name = "order_fulfillment_id", referencedColumnName = "id"))
})
public class OrderFulfillment extends OrderEntity<Long> {

    private String source;

    @Column(name="ecommerce_purchase_id")
    private Long ecommercePurchaseId;

    @Column(name="tracker_id")
    private Long trackerId;

    @Column(name="external_purchase_id")
    private Long externalPurchaseId;

    @Column(name="bridge_purchase_id")
    private Long bridgePurchaseId;

    @Column(name="delivery_cost")
    private BigDecimal deliveryCost;

    @Column(name="total_cost")
    private BigDecimal totalCost;

    @Column(name="created_order")
    private LocalDateTime createdOrder;

    @Column(name="scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name="confirmed_order")
    private LocalDateTime confirmedOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    private String notes;

    @Embedded
    private PaymentMethod paymentMethod;

    @Embedded
    private ReceiptType receiptType;

    @Embedded
    private Address address;

    @ElementCollection
    @CollectionTable(name = "order_fulfillment_item", joinColumns = @JoinColumn(name = "order_fulfillment_id"))
    private List<OrderFulfillmentItem> orderItem;

}