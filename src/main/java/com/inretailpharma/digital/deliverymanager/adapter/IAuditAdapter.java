package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import reactor.core.publisher.Mono;

public interface IAuditAdapter {

    Mono<OrderCanonical> createAudit(OrderCanonical orderCanonical, String updateBy);

    Mono<OrderCanonical> updateAudit(OrderCanonical orderCanonical, String updateBy);

    Mono<OrderCanonical> createAuditOnlyMysql(OrderCanonical orderCanonical, String updateBy);

}
