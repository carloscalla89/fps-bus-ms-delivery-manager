package com.inretailpharma.digital.deliverymanager.util;

@FunctionalInterface
public interface LiquidationStatus {

    String process(String digitalStatus);

    default boolean evaluateToSent(String action) {

        if (Constant.ActionOrder.FILL_ORDER.name().equalsIgnoreCase(action)
                || Constant.ActionOrder.ATTEMPT_TRACKER_CREATE.name().equalsIgnoreCase(action)
                || Constant.ActionOrder.ATTEMPT_INSINK_CREATE.name().equalsIgnoreCase(action)
                || Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(action)
                || Constant.ActionOrder.REJECT_ORDER.name().equalsIgnoreCase(action)
                || Constant.ActionOrder.DELIVER_ORDER.name().equalsIgnoreCase(action)) {

            return true;
        }

        return false;
    }
}
