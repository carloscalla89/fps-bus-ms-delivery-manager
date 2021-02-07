package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;
@Data
@Slf4j
public class AdapterAbstract implements AdapterInterface{

    @Autowired
    protected OrderTransaction orderTransaction;

    @Autowired
    protected ObjectToMapper objectToMapper;

    @Qualifier("audit")
    @Autowired
    protected OrderExternalService orderExternalServiceAudit;

    @Override
    public Mono<OrderCanonical> sendOrderTracker(OrderExternalService orderExternalService, Long ecommercePurchaseId,
                                                 Long externalId, String statusDetail, String statusName,
                                                 String orderCancelCode, String orderCancelObservation,
                                                 String orderCancelAppType) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService,
                                                              Long ecommerceId, ActionDto actionDto, String company,
                                                              String serviceType, Long orderId, String orderCancelCode,
                                                              String orderCancelObservation, String orderCancelAppType) {
        return null;
    }




}
