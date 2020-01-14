package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.util.Constant;

public interface OrderExternalService {

    void sendOrder(OrderFulfillmentCanonical orderAuditCanonical);
    void updateOrder(OrderCanonical orderCanonical);
    OrderCanonical getResultfromExternalServices(Long ecommerceId, Constant.ActionOrder actionOrder);

}
