package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.repository.*;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

    private OrderRepository orderRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private OrderStatusRepository orderStatusRepository;
    private ServiceLocalOrderRepository serviceLocalOrderRepository;
    private CenterCompanyRepository centerCompanyRepository;
    private ClientRepository clientRepository;

    public OrderRepositoryServiceImpl(OrderRepository orderRepository,
                                      ServiceTypeRepository serviceTypeRepository,
                                      OrderStatusRepository orderStatusRepository,
                                      ServiceLocalOrderRepository serviceLocalOrderRepository,
                                      CenterCompanyRepository centerCompanyRepository,
                                      ClientRepository clientRepository) {
        this.orderRepository = orderRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.serviceLocalOrderRepository = serviceLocalOrderRepository;
        this.centerCompanyRepository = centerCompanyRepository;
        this.clientRepository = clientRepository;
    }

    @Override
    public OrderFulfillment createOrder(OrderFulfillment orderFulfillment) {
        return orderRepository.save(orderFulfillment);
    }


    @Override
    public ServiceType getServiceTypeByCode(String code) {
        return serviceTypeRepository.getOne(code);
    }

    @Override
    public OrderStatus getOrderStatusByCode(String code) {
        return orderStatusRepository.getOne(code);
    }

    @Override
    public ServiceLocalOrder saveServiceLocalOrder(ServiceLocalOrder serviceLocalOrder) {
        return serviceLocalOrderRepository.save(serviceLocalOrder);
    }

    @Override
    public List<IOrderFulfillment> getListOrdersByStatus(Set<String> status) {
        return orderRepository.getListOrdersByStatus(status);
    }

    @Override
    public List<IOrderFulfillment> getListOrdersToCancel(String serviceType, String companyCode,
                                                         Integer maxDayPickup, String statustype) {
        return orderRepository.getListOrdersToCancel(serviceType, maxDayPickup,companyCode,
                new HashSet<>(Arrays.asList(statustype.split(",")))
        );
    }

    @Override
    public List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderFulfillmentId) {
        return orderRepository.getOrderItemByOrderFulfillmentId(orderFulfillmentId);
    }

    @Override
    public IOrderFulfillment getOrderByecommerceId(Long ecommerceId) {
        return orderRepository.getOrderByecommerceId(ecommerceId).stream().findFirst().orElse(null);
    }

    @Override
    public void updateRetryingOrderStatusProcess(Long orderFulfillmentId, Integer attemptTracker,
                                          Integer attempt, String orderStatusCode, String statusDetail) {
        serviceLocalOrderRepository.updateRetryingOrderStatusProcess(orderFulfillmentId, attemptTracker, attempt,
                orderStatusCode, statusDetail);
    }

    @Override
    public void updateReattemtpTracker(Long orderFulfillmentId, Integer attemptTracker,
                                       String orderStatusCode, String statusDetail) {
        serviceLocalOrderRepository.updateReattemtpTracker(orderFulfillmentId, attemptTracker,
                orderStatusCode, statusDetail);
    }


    @Override
    public void updateTrackerId(Long orderFulfillmentId, Long trackerId) {
        orderRepository.updateTrackerId(orderFulfillmentId, trackerId);
    }

    @Override
    public void updateExternalAndTrackerId(Long orderFulfillmentId, Long externalPurchaseId,
                                                     Long ecommerceId) {
        orderRepository.updateExternalAndTrackerId(orderFulfillmentId, externalPurchaseId, ecommerceId);
    }

    @Override
    public void updateExternalIdToReservedOrder(Long orderFulfillmentId, Long externalPurchaseId) {
        orderRepository.updateExternalIdToReservedOrder(orderFulfillmentId, externalPurchaseId);
    }

    @Override
    public void updateStatusToReservedOrder(Long orderFulfillmentId, Integer attempt, String orderStatusCode,
                                            String statusDetail) {
        serviceLocalOrderRepository.updateStatusToReservedOrder(orderFulfillmentId, attempt, orderStatusCode, statusDetail);
    }

    @Override
    public void updateStatusOrder(Long orderFulfillmentId, String orderStatusCode, String statusDetail) {
        serviceLocalOrderRepository.updateStatusOrder(orderFulfillmentId, orderStatusCode, statusDetail);
    }

    @Override
    public void updateStatusCancelledOrder(String statusDetail, String cancellationObservation, String cancellationCode,
                                           String cancellationAppType, String orderStatusCode, Long orderFulfillmentId) {
        serviceLocalOrderRepository.updateStatusCancelledOrder(statusDetail, cancellationObservation, cancellationCode,
                cancellationAppType, orderStatusCode, orderFulfillmentId);
    }

    @Override
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public List<OrderStatus> getOrderStatusByTypeIs(String statusName) {
        return orderStatusRepository.getOrderStatusByTypeIs(statusName);
    }
}
