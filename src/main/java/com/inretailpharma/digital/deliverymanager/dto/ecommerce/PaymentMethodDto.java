package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class PaymentMethodDto {

    @NotNull
    private Integer id;
    @NotBlank
    private String name;

    @Override
    public String toString() {
        return "PaymentMethodDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}


