package com.inretailpharma.digital.deliverymanager.canonical.manager;

import com.inretailpharma.digital.deliverymanager.dto.OderDetailOut;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderHeaderDetail {
    private String orderStatus;
    private String localId;
    private String serviceTypeId;
    private String documentoId;
    private String client;
    private String promiseDate;
    private Long orderId;
    private Long ecommerceId;
    private String ecommerceIdCall;
    private String companyCode;
    private String serviceChannel;
    private String source;
    private String orderType;
    private String serviceTypeShortCode;
    private LocalDateTime scheduledTime;
    private String statusName;
    private String statusCode;
    private String localCode;
    private String clientName;
    private String documentNumber;
    private String phone;
    private String email;
    private String addressClient;
    private String coordinates;
    private String reference;
    private String ruc;
    private String companyName;
    private String serviceType;
    private String purcharseId;
    private String observation;
    private String cancelReason;
    private String zoneId;
    private String stockType;
    private OderDetailOut oderDetailOut;
    //private OrderInfoConsolidated orderInfoConsolidated;

}
