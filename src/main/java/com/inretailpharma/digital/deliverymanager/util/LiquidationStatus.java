package com.inretailpharma.digital.deliverymanager.util;

@FunctionalInterface
public interface LiquidationStatus {

    String process(String liquidationStatus, String firstDigitalStatus, String action, String orderCancelCode,
                   String serviceType);

}
