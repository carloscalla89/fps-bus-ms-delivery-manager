package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderDto {

    @NotBlank
    @Pattern(regexp = "^[0-9]*$")
    private String id;
    @NotNull
    private Date dateCreated;
    @NotNull
    private Date orderDate;
    @NotBlank
    private String source;
    @NotBlank
    private String companyCode;
    @NotNull
    @Valid
    private UserDto user;
    @NotNull
    @Valid
    private PaymentMethodDto paymentMethod;
    @NotNull
    @Valid
    private ReceiptDto receipt;
    @NotNull
    @Valid
    private AddressDto deliveryAddress;
    @NotNull
    @Valid
    private PaymentDto paymentAmountDto;
    @NotEmpty
    @Valid
    private List<ItemDto> items;
    @NotNull
    private Integer creditCardProviderId;
    @NotNull
    private BigDecimal discountApplied;
    @NotBlank
    private String deliveryType;
    @NotNull
    @Min(0)
    private BigDecimal amount;
    private String marketplaceName;
    private String deliveryServiceName;

    private DrugstoreDto drugstore;

    private Long zoneId;
    private String districtCode;
    private Integer deliveryTime;

}
