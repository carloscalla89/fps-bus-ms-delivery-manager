package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("auditadapter")
public class AuditAdapterImpl extends AdapterAbstract implements AdapterInterface {

    private OrderExternalService orderExternalServiceAudit;
    private ObjectToMapper objectToMapper;

    @Autowired
    public AuditAdapterImpl(@Qualifier("audit")OrderExternalService orderExternalServiceAudit,
                            ObjectToMapper objectToMapper) {

        this.orderExternalServiceAudit = orderExternalServiceAudit;
        this.objectToMapper = objectToMapper;
    }

    @Override
    public Mono<Void> updateExternalAudit(boolean sendNewAudit, OrderCanonical orderAudit, String updateBy) {

        if (sendNewAudit) {
            log.info("Send to create to new audit history");
            orderExternalServiceAudit
                    .updateOrderNewAudit(objectToMapper.getAuditHistoryDtoFromObject(orderAudit, updateBy))
                    .subscribe();

        }

        return orderExternalServiceAudit.sendOrderReactive(orderAudit);

    }

    @Override
    public Mono<Void> createExternalAudit(boolean sendNewAudit, OrderCanonical orderAudit, String updateBy) {

        if (sendNewAudit) {
            log.info("Send to update to new audit history");
            orderExternalServiceAudit
                    .updateOrderNewAudit(objectToMapper.getAuditHistoryDtoFromObject(orderAudit, updateBy))
                    .subscribe();

        }

        return orderExternalServiceAudit.updateOrderReactive(orderAudit);
    }
}
