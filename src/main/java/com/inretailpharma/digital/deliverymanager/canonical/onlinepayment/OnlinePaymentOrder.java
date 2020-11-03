package com.inretailpharma.digital.deliverymanager.canonical.onlinepayment;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OnlinePaymentOrder  implements Serializable {
    String ecommerceExternalId;
}
