package com.inretailpharma.digital.deliverymanager.service.impl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.inretailpharma.digital.deliverymanager.entity.Client;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.deliverymanager.entity.ServiceType;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import com.inretailpharma.digital.deliverymanager.repository.ClientRepository;
import com.inretailpharma.digital.deliverymanager.repository.OrderRepository;
import com.inretailpharma.digital.deliverymanager.repository.OrderStatusRepository;
import com.inretailpharma.digital.deliverymanager.repository.ServiceLocalOrderRepository;
import com.inretailpharma.digital.deliverymanager.repository.ServiceTypeRepository;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

    private OrderRepository orderRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private OrderStatusRepository orderStatusRepository;
    private ServiceLocalOrderRepository serviceLocalOrderRepository;
    private ClientRepository clientRepository;

    public OrderRepositoryServiceImpl(OrderRepository orderRepository,
                                      ServiceTypeRepository serviceTypeRepository,
                                      OrderStatusRepository orderStatusRepository,
                                      ServiceLocalOrderRepository serviceLocalOrderRepository,
                                      ClientRepository clientRepository) {
        this.orderRepository = orderRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.serviceLocalOrderRepository = serviceLocalOrderRepository;
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

	@Override
	public <T> Optional<IOrderResponseFulfillment> getOrderByOrderNumber(Long orderNumber) {
		log.info("CALL Repository--getOrderByOrderNumber:"+orderNumber);
		return orderRepository.getOrderByOrderNumber(orderNumber);
	} 
}
