package com.inretailpharma.digital.ordermanager.proxy;

import com.inretailpharma.digital.ordermanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.ordermanager.config.parameters.ExternalServicesProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("auditProxy")
public class OrderAuditProxy implements  OrderAuditService{

    private ExternalServicesProperties externalServicesProperties;

    private static OrderAuditService orderAuditService;

    public OrderAuditProxy(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public void sendOrder(OrderFulfillmentCanonical orderAuditCanonical) {

        if (orderAuditService == null) {
            orderAuditService = new OrderAuditServiceImpl(externalServicesProperties);
        }

        orderAuditService.sendOrder(orderAuditCanonical);

    }
}
