package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.repository.*;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;


@Slf4j
@Service
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

    private OrderRepository orderRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private LocalRepository localRepository;
    private OrderStatusRepository orderStatusRepository;
    private ServiceLocalOrderRepository serviceLocalOrderRepository;

    public OrderRepositoryServiceImpl(OrderRepository orderRepository,
                                      ServiceTypeRepository serviceTypeRepository,
                                      LocalRepository localRepository,
                                      OrderStatusRepository orderStatusRepository,
                                      ServiceLocalOrderRepository serviceLocalOrderRepository) {
        this.orderRepository = orderRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.localRepository = localRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.serviceLocalOrderRepository = serviceLocalOrderRepository;
    }

    @Override
    public OrderFulfillment createOrder(OrderFulfillment orderFulfillment, OrderDto orderDto) {
        log.info("[START] create repository service");
        return orderRepository.save(orderFulfillment);
    }

    @Override
    public ServiceType getServiceTypeByCode(String code) {
        return serviceTypeRepository.getOne(code);
    }

    @Override
    public Local getLocalByCode(String localCode) {
        return localRepository.getOne(localCode);
    }

    @Override
    public Local getLocalByLocalCodeAndCompanyCode(String localCode, String companyCode) {
        return localRepository.getLocalByLocalIdentityCodeAndLocalIdentityCompany_Code(localCode, companyCode);
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
    public IOrderFulfillment getOrderByecommerceId(Long ecommerceId) {
        return orderRepository.getOrderByecommerceId(ecommerceId);
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

}
