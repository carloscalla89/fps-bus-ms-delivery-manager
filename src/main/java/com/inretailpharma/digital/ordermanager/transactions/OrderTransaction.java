package com.inretailpharma.digital.ordermanager.transactions;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.entity.*;
import com.inretailpharma.digital.ordermanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.ordermanager.service.OrderRepositoryService;
import com.inretailpharma.digital.ordermanager.util.Constant;
import com.inretailpharma.digital.ordermanager.util.DateUtils;
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

        /*
        serviceLocalOrderIdentity.setLocalCode(orderDto.getLocalCode());
        serviceLocalOrderIdentity.setOrderTrackerId(orderFulfillmentResp.getId());
        serviceLocalOrderIdentity.setServiceTypeCode(orderDto.getServiceTypeCode());
         */

        Local local = new Local();
        local.setCode(orderDto.getLocalCode());
        serviceLocalOrderIdentity.setLocal(orderRepositoryService.getLocalByCode(orderDto.getLocalCode()));
        //serviceLocalOrderIdentity.setLocal(local);

        ServiceType serviceType = new ServiceType();
        serviceType.setCode(orderDto.getServiceTypeCode());
        //serviceLocalOrderIdentity.setServiceType(serviceType);
        serviceLocalOrderIdentity.setServiceType(orderRepositoryService.getServiceTypeByCode(orderDto.getServiceTypeCode()));
        serviceLocalOrderIdentity.setOrderFulfillment(orderFulfillmentResp);


        // Set status from delivery dispatcher
        setStatusOrderFromDeliveryDispatcher(serviceLocalOrderIdentity, orderDto);
        // ----------------------------------------------------

        // Create and set object ServiceLocalOrder
        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();
        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);
        serviceLocalOrder.setDaysToPickup(0);
        serviceLocalOrder.setStartHour(DateUtils.getLocalTimeFromStringWithFormat("09:00:00"));
        serviceLocalOrder.setEndHour(DateUtils.getLocalTimeFromStringWithFormat("20:00:00"));
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

            //serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.SUCCESS_TRACKED_BILLED_ORDER.getCode());

            //orderStatus.setCode(Constant.OrderStatus.SUCCESS_TRACKED_BILLED_ORDER.getCode());
            //orderStatus.setType(Constant.OrderStatus.SUCCESS_TRACKED_BILLED_ORDER.name());
            /*
            serviceLocalOrderIdentity.setOrderStatus(
                    orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_TRACKED_BILLED_ORDER.getCode())
            );

             */

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_TRACKED_BILLED_ORDER.getCode());

        } else if (
                Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1") && orderDto.getTrackerId() != null) {

            //serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.getCode());

            //orderStatus.setCode(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.getCode());
            //orderStatus.setType(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.name());
            /*
            serviceLocalOrderIdentity.setOrderStatus(
                    orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.getCode())
            );

             */

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_RESERVED_ORDER.getCode());
        } else if (
                !Optional
                        .ofNullable(orderDto.getOrderStatusDto().getCode())
                        .orElse("OK")
                        .equalsIgnoreCase("0-1")  && orderDto.getTrackerId() != null) {

            //serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode());

            //orderStatus.setCode(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode());
            //orderStatus.setType(Constant.OrderStatus.ERROR_RESERVED_ORDER.name());
            /*
            serviceLocalOrderIdentity.setOrderStatus(
                    orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode())
            );

             */
            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode());
        } else if (orderDto.getExternalPurchaseId() != null){
            //serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
            /*
            orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
            orderStatus.setType(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());

            */
            /*
            serviceLocalOrderIdentity.setOrderStatus(
                    orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode())
            );

             */

            orderStatus =  orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());

        } else if (orderDto.getTrackerId() != null) {
            //serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());

            //orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
            //orderStatus.setType(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name());

            /*
            serviceLocalOrderIdentity.setOrderStatus(
                    orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
            );

             */
            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        } else {
            //serviceLocalOrderIdentity.setOrderStatusCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());

            //orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
            //orderStatus.setCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.name());
            /*
            serviceLocalOrderIdentity.setOrderStatus(
                    orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode())
            );

             */

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
        }

        serviceLocalOrderIdentity.setOrderStatus(orderStatus);
    }

}
