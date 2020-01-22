package com.inretailpharma.digital.deliverymanager.transactions;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
@Slf4j
@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
@Component
public class OrderTransaction {

    private OrderRepositoryService orderRepositoryService;


    public OrderTransaction(OrderRepositoryService orderRepositoryService) {
        this.orderRepositoryService = orderRepositoryService;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public ServiceLocalOrder createOrder(OrderFulfillment orderFulfillment, OrderDto orderDto) {

        OrderFulfillment orderFulfillmentResp = orderRepositoryService.createOrder(orderFulfillment, orderDto);

        // Set Object ServiceLocalOrderIdentity
        ServiceLocalOrderIdentity serviceLocalOrderIdentity = new ServiceLocalOrderIdentity();

        serviceLocalOrderIdentity.setLocal(
                Optional
                        .ofNullable(orderRepositoryService.getLocalByCode(orderDto.getLocalCode()))
                        .orElse(orderRepositoryService.getLocalByCode(Constant.Constans.NOT_DEFINED_LOCAL))
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
        serviceLocalOrder.setDaysToPickup(0);

        // Set attempt of attempt to insink and tracker
        serviceLocalOrder.setAttempt(Constant.Constans.ONE_ATTEMPT);
        if (!(serviceLocalOrderIdentity.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
            || serviceLocalOrderIdentity.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode()))) {
            serviceLocalOrder.setAttemptTracker(Constant.Constans.ONE_ATTEMPT);
        }

        Optional
                .ofNullable(orderDto.getOrderStatusDto())
                .ifPresent(r -> serviceLocalOrder.setStatusDetail(r.getDescription()));

        orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);


        return serviceLocalOrder;
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

}
