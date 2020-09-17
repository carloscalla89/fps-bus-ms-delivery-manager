package com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.PaymentMethodInkatrackerCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ReceiptInkatrackerCanonical;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfoInkatrackerLiteCanonical {

    @NotNull
    private Long orderExternalId;
    @NotNull
    @Pattern(regexp = "^(WEB|IOS|APP|CALL)$", message = "Accepted values: WEB, CALL, IOS, APP")
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
    private ClientInkatrackerLiteCanonical client;
    @NotNull
    @Valid
    private AddressInkatrackerLiteCanonical address;
    @NotNull
    private Long drugstoreId;

    private PaymentMethodInkatrackerCanonical paymentMethod;
    private ReceiptInkatrackerCanonical receipt;

    @NotNull
    @Valid
    private List<OrderItemInkatrackerLiteCanonical> orderItems;
    private String motorizedId;
    private String notes;
    @NotNull
    private Long deliveryServiceId;
    private String cancelMessageNote;
    private String cancelReasonCode;
    private StatusInkatrackerLiteCanonical status;
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
    private String drugstoreAddress;
    private String localCode;
    private String companyCode;
    private String newUserId;

}
