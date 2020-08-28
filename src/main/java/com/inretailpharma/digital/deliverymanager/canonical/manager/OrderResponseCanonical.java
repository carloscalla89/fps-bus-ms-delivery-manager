package com.inretailpharma.digital.deliverymanager.canonical.manager;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponseCanonical {
	
	//private Long orderNumber;
	private String payOrderDate;
    private String transactionOrderDate;
    private String purchaseNumber;
}
