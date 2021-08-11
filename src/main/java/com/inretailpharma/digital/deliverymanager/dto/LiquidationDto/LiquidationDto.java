package com.inretailpharma.digital.deliverymanager.dto.LiquidationDto;


import lombok.Data;

import java.math.BigDecimal;


@Data
public class LiquidationDto {
  //  @NotBlank(message = "ecommerceId may not be blank")
    private String ecommerceId;
    private String purchaseNumber;
    private String transactionVisanet;
    private String transactionVisanetDate;
    private String channel;
    private String source;
    private String localCode;
    private String companyCode;
   // @NotBlank(message = "serviceTypeCode may not be blank")
    private String serviceTypeCode;
  //  @NotBlank(message = "localType may not be blank")
    private String localType;
  //  @NotBlank(message = "serviceType may not be blank")
    private String serviceType;
   // @NotNull(message = "status may not be null")
    private StatusDto status;
   // @NotNull(message = "totalAmount may not be null")
    private BigDecimal totalAmount;
    private BigDecimal changeAmount;
   // @NotBlank(message = "paymentMethod may not be blank")
    private String paymentMethod;
    private String cardProvider;

    private String fullName;
    private String phone;
    private String documentNumber;

    private String origin;
}
