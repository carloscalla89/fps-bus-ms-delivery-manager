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
    private boolean externalRouting;

    public OrderCanonical(OrderCanonical orderCanonical) {
        this.id = orderCanonical.getId();
        this.ecommerceId = orderCanonical.getEcommerceId();
        this.trackerId = orderCanonical.getTrackerId();
        this.externalId = orderCanonical.getExternalId();
        this.purchaseId = orderCanonical.getPurchaseId();
        this.motorizedId = orderCanonical.getMotorizedId();
        this.deliveryCost = orderCanonical.getDeliveryCost();
        this.discountApplied = orderCanonical.getDiscountApplied();
        this.subTotalCost = orderCanonical.getSubTotalCost();
        this.totalAmount = orderCanonical.getTotalAmount();
        this.totalCostNoDiscount = orderCanonical.getTotalCostNoDiscount();
        this.localType = orderCanonical.getLocalType();
        this.companyCode = orderCanonical.getCompanyCode();
        this.localCode = orderCanonical.getLocalCode();
        this.local = orderCanonical.getLocal();
        this.company = orderCanonical.getCompany();
        this.localDescription = orderCanonical.getLocalDescription();
        this.localAddress = orderCanonical.getLocalAddress();
        this.localLongitude = orderCanonical.getLocalLongitude();
        this.localLatitude = orderCanonical.getLocalLatitude();
        this.inkaVentaId = orderCanonical.getInkaVentaId();
        this.localId = orderCanonical.getLocalId();
        this.storeCenter = orderCanonical.getStoreCenter();
        this.client = orderCanonical.getClient();
        this.orderStatus = new OrderStatusCanonical(orderCanonical.getOrderStatus());
        this.receipt = orderCanonical.getReceipt();
        this.paymentMethod = orderCanonical.getPaymentMethod();
        this.address = orderCanonical.getAddress();
        this.orderItems = orderCanonical.getOrderItems();
        this.orderDetail = orderCanonical.getOrderDetail();
        this.shelfList = orderCanonical.getShelfList();
        this.payBackEnvelope = orderCanonical.getPayBackEnvelope();
        this.attempt = orderCanonical.getAttempt();
        this.attemptTracker = orderCanonical.getAttemptTracker();
        this.source = orderCanonical.getSource();
        this.target = orderCanonical.getTarget();
        this.action = orderCanonical.getAction();
        this.partial = orderCanonical.getPartial();
        this.updateBy = orderCanonical.getUpdateBy();
        this.liquidation = orderCanonical.getLiquidation();
        this.groupId = orderCanonical.getGroupId();
        this.saleChannel = orderCanonical.getSaleChannel();
        this.saleChannelType = orderCanonical.getSaleChannelType();
    }
}
