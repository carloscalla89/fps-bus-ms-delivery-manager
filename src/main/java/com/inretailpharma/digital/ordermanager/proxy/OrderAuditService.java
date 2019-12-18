package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.audit.OrderAuditCanonical;

public interface OrderAuditService {

    void sendOrder(OrderAuditCanonical orderAuditCanonical);
}
