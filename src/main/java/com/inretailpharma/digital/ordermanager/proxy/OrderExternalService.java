package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.canonical.manager.OrderManagerCanonical;
import com.inretailpharma.digital.ordermanager.util.Constant;

public interface OrderExternalService {

    void sendOrder(OrderFulfillmentCanonical orderAuditCanonical);
    void updateOrder(OrderManagerCanonical orderManagerCanonical);
    OrderManagerCanonical getResultfromExternalServices(Long ecommerceId, Constant.ActionOrder actionOrder);

}
