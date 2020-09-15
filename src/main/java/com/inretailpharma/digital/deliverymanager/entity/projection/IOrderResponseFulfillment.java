package com.inretailpharma.digital.deliverymanager.entity.projection;

public interface IOrderResponseFulfillment {

    String getScheduledOrderDate();
    String getPayOrderDate();
    String getTransactionOrderDate();
    String getPurchaseNumber();
    String getPosCode();
    Long getPaymentMethodId();
    Long getCreditCardId();
    String getConfirmedOrder();
}