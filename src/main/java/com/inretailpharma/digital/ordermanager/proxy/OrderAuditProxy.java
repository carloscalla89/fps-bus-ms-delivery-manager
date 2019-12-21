package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.audit.OrderAuditCanonical;


public class OrderAuditProxy implements  OrderAuditService{
    private static OrderAuditService orderAuditService;

    @Override
    public void sendOrder(OrderAuditCanonical orderAuditCanonical) {

        if (orderAuditService == null) {
            orderAuditService = new OrderAuditServiceImpl();
        }

        orderAuditService.sendOrder(orderAuditCanonical);

    }
}
