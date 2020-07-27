package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class OrderDto {

    @NotBlank
    @Pattern(regexp = "^[0-9]*$")
    private String id;
    @NotNull
    private Date dateCreated;
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
    private Double discountApplied;
    @NotBlank
    private String deliveryType;
    @NotNull
    @Min(0)
    private Double amount;
    private String marketplaceName;

    @Override
    public String toString() {
        return "OrderDto{" +
                "id='" + id + '\'' +
                ", dateCreated=" + dateCreated +
                ", source='" + source + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", user=" + user +
                ", paymentMethod=" + paymentMethod +
                ", receipt=" + receipt +
                ", deliveryAddress=" + deliveryAddress +
                ", paymentAmountDto=" + paymentAmountDto +
                ", items=" + items +
                ", creditCardProviderId=" + creditCardProviderId +
                ", discountApplied=" + discountApplied +
                ", deliveryType='" + deliveryType + '\'' +
                ", amount=" + amount +
                ", marketplaceName='" + marketplaceName + '\'' +
                '}';
    }
}