package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class ReceiptTypeDto {

    @NotBlank
    @Pattern(regexp = "^(TICKET|INVOICE)$")
    private String name;

    @Override
    public String toString() {
        return "ReceiptTypeDto{" +
                "name='" + name + '\'' +
                '}';
    }
}
