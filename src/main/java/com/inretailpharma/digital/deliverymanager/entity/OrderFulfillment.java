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

	private static final long serialVersionUID = 1L;

	private String source;

    @Column(name="ecommerce_purchase_id")
    private Long ecommercePurchaseId;

    @Column(name="tracker_id")
    private Long trackerId;

    @Column(name="external_purchase_id")
    private Long externalPurchaseId;

    @Column(name="purchase_number")
    private Integer purchaseNumber;

    @Column(name="delivery_cost")
    private BigDecimal deliveryCost;

    @Column(name="discount_applied")
    private BigDecimal discountApplied;

    @Column(name="total_cost_no_discount")
    private BigDecimal totalCostNoDiscount;

    @Column(name="sub_total_cost")
    private BigDecimal subTotalCost; // gross price

    @Column(name="total_cost")
    private BigDecimal totalCost;

    @Column(name="created_order")
    private LocalDateTime createdOrder;

    @Column(name="scheduled_time")
    private LocalDateTime scheduledTime;

    @Column(name="confirmed_order")
    private LocalDateTime confirmedOrder;

    @Column(name="confirmed_insink_order")
    private LocalDateTime confirmedInsinkOrder;

    @Column(name="cancelled_order")
    private LocalDateTime cancelledOrder;

    @Column(name="transaction_order_date")
    private String transactionOrderDate;

    @Column(name="source_company_name")
    private String sourceCompanyName;

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

    private Integer partial;

    @ElementCollection
    @CollectionTable(name = "order_fulfillment_item", joinColumns = @JoinColumn(name = "order_fulfillment_id"))
    private List<OrderFulfillmentItem> orderItem;

    @Column(name="pos_code")
    private String posCode;
}
