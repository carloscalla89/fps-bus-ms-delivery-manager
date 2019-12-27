package com.inretailpharma.digital.ordermanager.transactions;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.entity.*;
import com.inretailpharma.digital.ordermanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.ordermanager.service.OrderRepositoryService;
import com.inretailpharma.digital.ordermanager.util.Constant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        Local local = new Local();
        local.setCode(orderDto.getLocalCode());
        serviceLocalOrderIdentity.setLocal(orderRepositoryService.getLocalByCode(orderDto.getLocalCode()));

        ServiceType serviceType = new ServiceType();
        serviceType.setCode(orderDto.getServiceTypeCode());

        serviceLocalOrderIdentity.setServiceType(orderRepositoryService.getServiceTypeByCode(orderDto.getServiceTypeCode()));
        serviceLocalOrderIdentity.setOrderFulfillment(orderFulfillmentResp);


        // Set status from delivery dispatcher
        setStatusOrderFromDeliveryDispatcher(serviceLocalOrderIdentity, orderDto);
        // ----------------------------------------------------

        // Create and set object ServiceLocalOrder
        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();
        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);
        serviceLocalOrder.setDaysToPickup(0);
        serviceLocalOrder.setAttempt(Constant.Constans.ONE_ATTEMPT);
        Optional
                .ofNullable(orderDto.getOrderStatusDto())
                .ifPresent(r -> serviceLocalOrder.setStatusDetail(r.getDescription()));


        orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);


        return serviceLocalOrder;
    }

    public List<IOrderFulfillment> getListOrdersByStatus(Set<String> status){
        return orderRepositoryService.getListOrdersByStatus(status);
    }

    private void setStatusOrderFromDeliveryDispatcher(ServiceLocalOrderIdentity serviceLocalOrderIdentity,
                                                      OrderDto orderDto) {
        OrderStatus orderStatus;

        // set status

        if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.FULFILLMENT_PROCESS_SUCCESS.getCode());

        } else if (
                Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1") && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.getCode());
        } else if (
                !Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1")
                        && orderDto.getProgrammed() && orderDto.getTrackerId() != null) {


            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode());
        } else if (orderDto.getExternalPurchaseId() != null){
            orderStatus =  orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());

        } else if (orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        } else {
            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
        }

        serviceLocalOrderIdentity.setOrderStatus(orderStatus);
    }

    public IOrderFulfillment getOrderByecommerceId(Long ecommerceId) {
        return orderRepositoryService.getOrderByecommerceId(ecommerceId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateReattemtpInsink(Long orderFulfillmentId, Integer attempt,
                                      String orderStatusCode, String statusDetail){
        orderRepositoryService.updateReattemtpInsink(
                orderFulfillmentId, attempt, orderStatusCode, statusDetail
        );
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateExternalPurchaseId(Long orderFulfillmentId, Long externalPurchaseId) {
        orderRepositoryService.updateExternalPurchaseId(orderFulfillmentId, externalPurchaseId);
    }

}
