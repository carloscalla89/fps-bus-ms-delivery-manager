package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;

import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.GroupCanonical;
import lombok.Data;

import java.util.List;

@Data
public class OrderInkatrackerCanonical {

    private Long orderExternalId;
    private OrderStatusInkatrackerCanonical orderStatus;
    private Long inkaDeliveryId;
    private String source;
    private Long dateCreated;
    private Integer deliveryService;
    private Long maxDeliveryTime;
    private Double deliveryCost;
    private Double totalCost;
    private Double subtotal;
    private ClientInkatrackerCanonical client;
    private AddressInkatrackerCanonical address;
    private DrugstoreCanonical drugstore;
    private Long drugstoreId;
    private PaymentMethodInkatrackerCanonical paymentMethod;
    private List<OrderItemInkatrackerCanonical> orderItems;
    private GroupCanonical group;
    private String motorizedId;
    private String statusDrugstore;
    private ScheduledCanonical scheduled;
    private Integer eta;
    private String note;
    private List<PreviousStatusCanonical> previousStatus;
    private ReceiptInkatrackerCanonical receipt;
    private Double discountApplied;
    private String deliveryType;
    private String startHour;
    private String endHour;
    private String daysToPickUp;
    private String messageToPickup;
    private String companyCode;
    private String companyName;
    private String errorDetail;
    private Long responseExternalId;
    private String responseExternalCode;

    private DrugstoreCanonical drugstoreSource;
    private boolean assignedDrugstoreNew;
    private String priority;
}
