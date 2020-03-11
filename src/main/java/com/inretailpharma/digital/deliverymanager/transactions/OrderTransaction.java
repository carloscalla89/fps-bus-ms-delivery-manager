package com.inretailpharma.digital.deliverymanager.transactions;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
@Component
public class OrderTransaction {

    private OrderRepositoryService orderRepositoryService;
    private OrderCancellationService orderCancellationService;

    public OrderTransaction(OrderRepositoryService orderRepositoryService,
                            OrderCancellationService orderCancellationService) {
        this.orderRepositoryService = orderRepositoryService;
        this.orderCancellationService = orderCancellationService;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public OrderWrapperResponse createOrderTransaction(OrderFulfillment orderFulfillment, OrderDto orderDto) {

        log.info("[START ] createOrderReactive");

        Client client = orderRepositoryService.saveClient(orderFulfillment.getClient());

        orderFulfillment.setClient(client);

        OrderFulfillment orderFulfillmentResp = orderRepositoryService.createOrder(orderFulfillment);

        // Set Object ServiceLocalOrderIdentity
        ServiceLocalOrderIdentity serviceLocalOrderIdentity = new ServiceLocalOrderIdentity();

        serviceLocalOrderIdentity.setCenterCompanyFulfillment(
                Optional
                        .ofNullable(orderRepositoryService.getCenterCompanyByCenterCodeAndCompanyCode(orderDto.getLocalCode(), orderDto.getCompanyCode()))
                        .orElse(orderRepositoryService.getCenterCompanyByCenterCodeAndCompanyCode(Constant.Constans.NOT_DEFINED_CENTER, Constant.Constans.NOT_DEFINED_COMPANY))
        );

        serviceLocalOrderIdentity.setServiceType(
                Optional
                        .ofNullable(orderRepositoryService.getServiceTypeByCode(orderDto.getServiceTypeCode()))
                        .orElse(orderRepositoryService.getServiceTypeByCode(Constant.Constans.NOT_DEFINED_SERVICE))

        );
        serviceLocalOrderIdentity.setOrderFulfillment(orderFulfillmentResp);

        // Set status from delivery dispatcher
        OrderStatus orderStatus = getStatusOrderFromDeliveryDispatcher(orderDto);
        serviceLocalOrderIdentity.setOrderStatus(orderStatus);
        // ----------------------------------------------------

        // Create and set object ServiceLocalOrder
        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();
        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);

        // Set attempt of attempt to insink and tracker
        serviceLocalOrder.setAttempt(Constant.Constans.ONE_ATTEMPT);

        if (!(serviceLocalOrderIdentity.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                || serviceLocalOrderIdentity.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode()))) {
            serviceLocalOrder.setAttemptTracker(Constant.Constans.ONE_ATTEMPT);
        }

        Optional
                .ofNullable(orderDto.getOrderStatusDto())
                .ifPresent(r -> serviceLocalOrder.setStatusDetail(r.getDescription()));

        ServiceLocalOrder serviceLocalOrderResponse =  orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);

        // Set the values of return of transaction as wrapped
        OrderWrapperResponse orderWrapperResponse = new OrderWrapperResponse();
        orderWrapperResponse.setFulfillmentId(orderFulfillmentResp.getId());
        orderWrapperResponse.setTrackerId(orderFulfillmentResp.getId());
        orderWrapperResponse.setOrderStatusCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getCode());
        orderWrapperResponse.setOrderStatusName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getType());
        orderWrapperResponse.setOrderStatusDetail(serviceLocalOrderResponse.getStatusDetail());
        orderWrapperResponse.setServiceCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getCode());
        orderWrapperResponse.setServiceName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getName());
        orderWrapperResponse.setServiceType(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getType());
        orderWrapperResponse.setAttemptBilling(serviceLocalOrderResponse.getAttempt());
        orderWrapperResponse.setAttemptTracker(serviceLocalOrderResponse.getAttemptTracker());
        orderWrapperResponse.setLocalName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getCenterCompanyFulfillment().getCenterName());
        orderWrapperResponse.setCompanyName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getCenterCompanyFulfillment().getCompanyName());
        orderWrapperResponse.setPaymentMethodName(orderFulfillmentResp.getPaymentMethod().getPaymentType().name());
        orderWrapperResponse.setReceiptName(orderFulfillmentResp.getReceiptType().getName());
        log.info("[END] createOrderReactive");

        return orderWrapperResponse;
    }


    public OrderStatus  getStatusOrderFromDeliveryDispatcher(OrderDto orderDto) {
        OrderStatus orderStatus;

        // set status

        if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS.getCode());

        } else if (
                Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1") && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.getCode());

        } else if (Optional
                .ofNullable(orderDto.getOrderStatusDto().getCode())
                .orElse("OK")
                .equalsIgnoreCase("0-1")
                && orderDto.getTrackerId() == null
                && Optional.ofNullable(orderDto.getProgrammed()).orElse(false)) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());

        } else if (
                !Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1")
                        && Optional.ofNullable(orderDto.getProgrammed()).orElse(false)
                        && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode());

        } else if (Optional
                .ofNullable(orderDto.getOrderStatusDto().getCode())
                .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.OrderStatus.ERROR_RELEASE_ORDER.name())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode());
        } else if(Optional
                .ofNullable(orderDto.getOrderStatusDto().getCode())
                .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.OrderStatus.ERROR_UPDATE_TRACKER_BILLING.name())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_UPDATE_TRACKER_BILLING.getCode());

        } else if (Optional
                    .ofNullable(orderDto.getOrderStatusDto().getCode())
                    .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)
                    && Optional
                        .ofNullable(orderDto.getPayment().getType())
                        .orElse(PaymentMethod.PaymentType.CASH.name())
                        .equalsIgnoreCase(PaymentMethod.PaymentType.ONLINE_PAYMENT.name())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.getCode());

        } else if (Optional
                .ofNullable(orderDto.getOrderStatusDto().getCode())
                .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CANCELLED_ORDER.getCode());

        } else if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId()==null){

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
        } else {
            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        }

        return orderStatus;

    }

    public IOrderFulfillment getOrderByecommerceId(Long ecommerceId) {
        return orderRepositoryService.getOrderByecommerceId(ecommerceId);
    }


    public List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderFulfillmentId) {
        return orderRepositoryService.getOrderItemByOrderFulfillmentId(orderFulfillmentId);
    }

    public List<IOrderFulfillment> getOrdersByStatus(String status){
        return orderRepositoryService.getListOrdersByStatus(new HashSet<>(Collections.singletonList(status)));
    }

    public List<IOrderFulfillment> getListOrdersToCancel(String statusName, String serviceType) {
        return orderRepositoryService.getListOrdersToCancel(new HashSet<>(Collections.singletonList(statusName)), serviceType);
    }

    public OrderFulfillment getOrderFulfillmentById(Long id) {
        return orderRepositoryService.getOrderFulfillmentById(id);
    }

    public List<OrderStatus> getOrderStatusByTypeIs(String statusName) {
        return orderRepositoryService.getOrderStatusByTypeIs(statusName);
    }



    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateOrderRetrying(Long orderFulfillmentId, Integer attempt, Integer attemptTracker,
                                    String orderStatusCode, String statusDetail, Long externalPurchaseId,
                                    Long trackerId){
        log.info("[START] updateOrderRetrying - orderFulfillmentId:{}, attempt:{}, attemptTracker:{}, " +
                        "orderStatusCode:{}, statusDetail:{}, externalPurchaseId:{}, trackerId:{}",
                orderFulfillmentId, attempt, attemptTracker, orderStatusCode, statusDetail, externalPurchaseId, trackerId);

        orderRepositoryService.updateExternalAndTrackerId(orderFulfillmentId, externalPurchaseId, trackerId);

        orderRepositoryService.updateRetryingOrderStatusProcess(
                orderFulfillmentId, attemptTracker, attempt, orderStatusCode, statusDetail
        );


    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateOrderRetryingTracker(Long orderFulfillmentId, Integer attemptTracker,
                                      String orderStatusCode, String statusDetail, Long trackerId){
        log.info("[START] updateReattemtpTracker - orderFulfillmentId:{}, attempt:{}, " +
                        "orderStatusCode:{}, statusDetail:{}, trackerId:{}",
                orderFulfillmentId, attemptTracker, orderStatusCode, statusDetail, trackerId);

        orderRepositoryService.updateTrackerId(orderFulfillmentId, trackerId);

        orderRepositoryService.updateReattemtpTracker(
                orderFulfillmentId, attemptTracker, orderStatusCode, statusDetail
        );
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateReservedOrder(Long orderFulfillmentId, Long externalPurchaseId, Integer attempt, String orderStatusCode,
                                    String statusDetail) {
        log.info("[START] updateReservedOrder - orderFulfillmentId:{} - externalPurchaseId:{} , attempt:{}, " +
                        "orderStatusCode:{}, statusDetail:{}",
                orderFulfillmentId, externalPurchaseId, attempt, orderStatusCode, statusDetail);

        orderRepositoryService.updateExternalIdToReservedOrder(orderFulfillmentId, externalPurchaseId);

        orderRepositoryService.updateStatusToReservedOrder(orderFulfillmentId, attempt, orderStatusCode, statusDetail);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateStatusOrder(Long orderFulfillmentId, String orderStatusCode, String statusDetail) {
        log.info("[START] updateOrderStatus - orderFulfillmentId:{} - orderStatusCode:{}, statusDetail:{}",
                orderFulfillmentId, orderStatusCode, statusDetail);

        orderRepositoryService.updateStatusOrder(orderFulfillmentId, orderStatusCode, statusDetail);
    }

    public List<CancellationCodeReason> getListCancelReason() {
        return orderCancellationService.getListCodeCancellationByCode();
    }

    public CancellationCodeReason getCancellationCodeReasonByCode(String code) {
        return orderCancellationService.geByCode(code);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void insertCancelledOrder(OrderCancelled orderCancelled) {
        log.info("[START] insertCancelledOrder transactional- orderCancelled-{}",orderCancelled);
        orderCancellationService.insertCancelledOrder(orderCancelled);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateStatusCancelledOrder(String statusDetail, String cancellationObservation,
                                           String orderStatusCode, Long orderFulfillmentId) {
        log.info("[START] updateStatusCancelledOrder transactional - statusDetail:{}, " +
                 "cancellationObservation:{},orderStatusCode:{}, orderFulfillmentId:{}"
                 ,statusDetail, cancellationObservation, orderStatusCode, orderFulfillmentId);

        orderRepositoryService.updateStatusCancelledOrder(statusDetail, cancellationObservation,
                orderStatusCode, orderFulfillmentId);
    }

}
