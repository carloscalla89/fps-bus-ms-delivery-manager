package com.inretailpharma.digital.deliverymanager.canonical.inkatracker;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfoCanonical {

    @NotNull
    private Long orderExternalId;
    @NotNull
    @Pattern(regexp = "^(WEB|IOS|APP|CALL|SC)$", message = "Accepted values: WEB, CALL, IOS, APP")
    private String source;
    @NotNull
    private Long dateCreated;
    private Long inkaDeliveryId;
    @NotNull
    private Long maxDeliveryTime;
    @NotNull
    private Double deliveryCost;
    @NotNull
    @Min(value = 0, message = "Total cost cannot be less than 0")
    private Double totalCost;
    @NotNull
    @Valid
    private ClientCanonical client;
    @NotNull
    @Valid
    private AddressCanonical address;
    @NotNull
    private Long drugstoreId;
    @NotNull
    @Valid
    private List<OrderItemInkatrackerCanonical> orderItems;
    private String motorizedId;
    private String note;
    @NotNull
    private Long deliveryService;
    private String cancelMessageNote;
    private String cancelReasonCode;
    private StatusCanonical status;
    private Long cancelDate;
    private Long startDate;
    private Long endDate;
    private BigDecimal discountApplied;
    private Double subtotal;
    private String callSource;
    private String codeMessage;
    private String descriptionMessage;
    private boolean isModified;
    private String deliveryType;
    private String startHour;
    private String endHour;
    private String daysToPickUp;
    private String purchaseId;
    private Drugstore drugstoreSource;
    private Drugstore drugstore;
    private OrderStatusInkatrackerCanonical orderStatus;
    private PaymentMethodInkatrackerCanonical paymentMethod;
    private ReceiptInkatrackerCanonical receipt;
    private String companyCode;
    private String sourceCompanyName;
    private ScheduledCanonical scheduled;
    private List<PreviousStatusCanonical> previousStatus;
}
