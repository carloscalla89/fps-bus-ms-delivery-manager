package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuditAdapter extends  AdapterAbstractUtil implements IAuditAdapter{

    private OrderExternalService orderExternalServiceAudit;

    @Autowired
    public AuditAdapter(@Qualifier("audit") OrderExternalService orderExternalServiceAudit) {
        this.orderExternalServiceAudit = orderExternalServiceAudit;
    }

    @Override
    public Mono<OrderCanonical> createAudit(OrderCanonical orderAudit, String updateBy) {

        orderExternalServiceAudit.createOrderNewAudit(getAuditHistoryDtoFromObject(orderAudit, updateBy)).subscribe();

        orderExternalServiceAudit.sendOrderReactive(orderAudit).subscribe();

        return Mono.just(orderAudit);
    }

    @Override
    public Mono<OrderCanonical> updateAudit(OrderCanonical orderAudit, String updateBy) {

        orderExternalServiceAudit.updateOrderNewAudit(getAuditHistoryDtoFromObject(orderAudit, updateBy)).subscribe();

        orderExternalServiceAudit.updateOrderReactive(orderAudit).subscribe();

        return Mono.just(orderAudit);
    }

    @Override
    public Mono<OrderCanonical> createAuditOnlyMysql(OrderCanonical orderAudit, String updateBy) {

        orderExternalServiceAudit.createOrderNewAudit(getAuditHistoryDtoFromObject(orderAudit, updateBy)).subscribe();

        return Mono.just(orderAudit);
    }
}
