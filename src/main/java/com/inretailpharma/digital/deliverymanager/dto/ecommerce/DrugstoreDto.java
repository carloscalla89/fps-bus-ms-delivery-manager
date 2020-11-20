package com.inretailpharma.digital.deliverymanager.dto.ecommerce;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DrugstoreDto {

    Long id;
    String inkaVentaId;
    String localCode;
    String name;
    String description;
    String address;
    String email;
    BigDecimal latitude;
    BigDecimal longitude;
    Integer warehouseId;

}
