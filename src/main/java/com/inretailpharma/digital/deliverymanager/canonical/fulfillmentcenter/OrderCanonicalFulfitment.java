package com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderCanonicalFulfitment {

    private String orderStatus;
    private String localId;
    private String companyCode;
    private String serviceChannel;
    private Long orderId;
    private Long ecommerceId;
    private String serviceTypeId;
    private String documentoId;
    private String razonSocial;
    private String fechaPromesa;
}
