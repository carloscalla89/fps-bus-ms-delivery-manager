package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.canonical.audit.OrderAuditCanonical;

public interface OrderAuditService {

    void sendOrder(OrderFulfillmentCanonical orderAuditCanonical);
}
