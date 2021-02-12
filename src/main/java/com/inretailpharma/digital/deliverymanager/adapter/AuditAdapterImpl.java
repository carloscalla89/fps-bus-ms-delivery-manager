package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("auditadapter")
public class AuditAdapterImpl extends AdapterAbstract implements AdapterInterface {

    @Qualifier("audit")
    @Autowired
    private OrderExternalService orderExternalServiceAudit;

    @Override
    public Mono<Void> updateExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical) {

        if (sendNewAudit) {

            orderExternalServiceAudit.updateOrderNewAudit(new AuditHistoryDto()).subscribe();

        }

        return orderExternalServiceAudit.updateOrderReactive(orderAuditCanonical);


    }
}
