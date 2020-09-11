package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponseCanonical {

    private String scheduledOrderDate;
	private String payOrderDate;
    private String transactionOrderDate;
    private String purchaseNumber;
    private String posCode;
    private Long paymentMethodId;
    private Long creditCardId;
}
