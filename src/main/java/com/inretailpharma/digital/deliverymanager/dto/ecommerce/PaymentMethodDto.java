package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PaymentMethodDto {

    @NotNull
    private Integer id;
    @NotBlank
    private String name;
    private Integer cardProviderId;
    private String cardProviderCode;
    private String cardProvider;
    private String bin;
    private String coupon;

}


