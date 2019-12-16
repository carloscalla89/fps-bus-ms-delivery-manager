package com.inretailpharma.digital.ordermanager.service.impl;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.entity.*;
import com.inretailpharma.digital.ordermanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.ordermanager.repository.LocalRepository;
import com.inretailpharma.digital.ordermanager.repository.OrderRepository;
import com.inretailpharma.digital.ordermanager.repository.ServiceLocalOrderRepository;
import com.inretailpharma.digital.ordermanager.repository.ServiceTypeRepository;
import com.inretailpharma.digital.ordermanager.service.OrderRepositoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Slf4j
@Service
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

    private OrderRepository orderRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private LocalRepository localRepository;
    private ServiceLocalOrderRepository serviceLocalOrderRepository;

    public OrderRepositoryServiceImpl(OrderRepository orderRepository,
                                      ServiceTypeRepository serviceTypeRepository,
                                      LocalRepository localRepository,
                                      ServiceLocalOrderRepository serviceLocalOrderRepository) {
        this.orderRepository = orderRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.localRepository = localRepository;
        this.serviceLocalOrderRepository = serviceLocalOrderRepository;
    }

    @Override
    public OrderFulfillment createOrder(OrderFulfillment orderFulfillment, OrderDto orderDto) {
        log.info("[START] create repository service");
        return orderRepository.save(orderFulfillment);
    }

    @Override
    public ServiceType getByCode(String code) {
        return serviceTypeRepository.getOne(code);
    }

    @Override
    public Local getByLocalCode(String localCode) {
        return localRepository.getOne(localCode);
    }

    //@Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    @Override
    public ServiceLocalOrder saveServiceLocalOrder(ServiceLocalOrder serviceLocalOrder) {
        return serviceLocalOrderRepository.save(serviceLocalOrder);
    }

    @Override
    public List<IOrderFulfillment> getListOrdersByStatus(String status) {
        return orderRepository.getListOrdersByStatus(status);
    }
}
