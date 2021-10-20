package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

import com.inretailpharma.digital.deliverymanager.util.DateUtils;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderCanonical {

    public OrderCanonical() {

    }

    public OrderCanonical(String code, String name) {
        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
    }

    public OrderCanonical(Long id,Long ecommerceId, OrderStatusCanonical orderStatus) {
        this.id = id;
        this.ecommerceId = ecommerceId;
        this.orderStatus = orderStatus;
    }

    public OrderCanonical(Long ecommerceId, String code, String name) {
        this.ecommerceId = ecommerceId;

        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
        this.orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
    }

    public OrderCanonical(Long ecommerceId, String code, String name, String localCode, String companyCode) {
        this.ecommerceId = ecommerceId;
        this.localCode = localCode;
        this.companyCode = companyCode;
        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
        this.orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
    }

    public OrderCanonical(Long ecommerceId, Long externalId, String code, String name) {
        this.ecommerceId = ecommerceId;
        this.externalId = externalId;
        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
        this.orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
    }

    public OrderCanonical(Long ecommerceId, String code, String name, String detail) {
        this.ecommerceId = ecommerceId;

        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
        this.orderStatus.setDetail(detail);
        this.orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
    }
    
    public OrderCanonical(Long ecommerceId, String code, String name, String localCode, String companyCode,
                          String source, String serviceTypeCode, String detail) {
        this.ecommerceId = ecommerceId;
        this.localCode = localCode;
        this.companyCode = companyCode;
        this.orderStatus = new OrderStatusCanonical();
        this.orderStatus.setCode(code);
        this.orderStatus.setName(name);
        this.orderStatus.setStatusDate(DateUtils.getLocalDateTimeNow());
        this.orderStatus.setDetail(detail);
        this.source = source;
        this.orderDetail = new OrderDetailCanonical();
        this.orderDetail.setServiceType(serviceTypeCode);
    }



    // Canonical IDs
    @JsonIgnore
    private Long id;
    private Long ecommerceId;
    private Long trackerId;
    private Long externalId;
    private Long purchaseId;
    private String motorizedId;

    // Canonical cost
    private BigDecimal deliveryCost;
    private BigDecimal discountApplied;
    private BigDecimal subTotalCost;
    private BigDecimal totalAmount;
    private BigDecimal totalCostNoDiscount;

    // Canonical local and company
    private String localType;
    private String companyCode;
    private String localCode;
    private String local;
    private String company;
    private String localDescription;
    private String localAddress;
    private BigDecimal localLongitude;
    private BigDecimal localLatitude;
    private String inkaVentaId;
    private Long localId;

    private StoreCenterCanonical storeCenter;

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

    private String source;
    private String target;

    private String action;
    private Boolean partial;

    private String updateBy;

    private LiquidationCanonical liquidation;
    private Long groupId;
    
    private String saleChannel;
    private String saleChannelType;

}
