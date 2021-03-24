package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

import java.util.List;

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
    public Mono<OrderCanonical> sendOrderTracker(OrderExternalService orderExternalService, StoreCenterCanonical storeCenter,
                                                 Long ecommercePurchaseId, Long externalId, String statusDetail, String statusName,
                                                 String orderCancelCode, String orderCancelObservation,
                                                 String orderCancelAppType) {
        return null;
    }

    @Override
    public Mono<OrderCanonical> getResultfromExternalServices(OrderExternalService orderExternalService,
                                                              Long ecommerceId, ActionDto actionDto, String company,
                                                              String serviceType, Long orderId, String orderCancelCode,
                                                              String orderCancelObservation, String orderCancelAppType,
                                                              String statusCode, String origin) {
        return null;
    }

    @Override
    public Mono<Void> createExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical, String updateBy) {
        return null;
    }

    @Override
    public Mono<Void> updateExternalAudit(boolean sendNewAudit, OrderCanonical orderAuditCanonical, String updateBy) {
        return null;
    }

    @Override
    public Mono<Boolean> sendNotification(String channel, String serviceTypeCode, String orderStatus, Long ecommerceId,
                                       String brand, String localCode, String localTypeCode, String phoneNumber,
                                       String clientName, String expiredDate, String confirmedDate, String address){
        return null;
    }

    @Override
    public Mono<OrderCanonical> getOrder(IOrderFulfillment iOrderFulfillment) {
        return null;
    }


}
