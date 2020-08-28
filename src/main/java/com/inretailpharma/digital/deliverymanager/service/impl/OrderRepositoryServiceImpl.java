package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderItemDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.repository.*;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    public boolean updatePartialOrderHeader(OrderDto orderDto) {
        BigDecimal totalCost = orderDto.getTotalCost();
        BigDecimal bigDecimal = orderDto.getDeliveryCost();
        LocalDateTime dateLastUpdated =  DateUtils.getLocalDateTimeObjectNow();
        Long externalPurchaseId = orderDto.getEcommercePurchaseId();

         orderRepository.updatePartialOrder(totalCost,bigDecimal,dateLastUpdated,externalPurchaseId,true);
         log.info("The order {} header was updated sucessfully",externalPurchaseId);
         return true;
    }

    @Override
    public boolean updatePartialOrderDetail(OrderDto orderDto, List<IOrderItemFulfillment> iOrderItemFulfillment) {

        for (IOrderItemFulfillment itemOriginal : iOrderItemFulfillment) {
            OrderItemDto itemDto = orderDto.getOrderItem().stream().filter(dto -> dto.getProductCode()
                    .equals(itemOriginal.getProductCode())).findFirst().orElse(null);
            if (itemDto == null) {
                log.info("The item {} of the order {} is removed because it does not exist in the list to update",
                        itemOriginal.getProductCode(),orderDto.getEcommercePurchaseId());

                deleteItemRetired(itemOriginal.getProductCode(), iOrderItemFulfillment.get(0).getOrderFulfillmentId());
            } else {
                if(itemDto.isEdited()){
                    Long orderFulfillmentId = itemOriginal.getOrderFulfillmentId();
                    String productCode = itemOriginal.getProductCode();
                    Integer quantity = itemDto.getQuantity();
                    BigDecimal unitPrice = itemDto.getUnitPrice();
                    BigDecimal totalPrice = itemDto.getTotalPrice();
                    Integer quantityUnits= itemDto.getQuantityUnits();
                    Constant.Logical fractionated = Constant.Logical.parse(itemDto.getFractionated());
                    orderRepository.updateItemsPartialOrder(quantity, unitPrice, totalPrice, fractionated.getValueString(), orderFulfillmentId,quantityUnits, productCode);
                }
            }
        }
        return true;
    }

    @Override
    public boolean deleteItemRetired(String itemsId,Long  orderFulfillmentId) {
        log.info("Deleting itemId: {} from orderId: {}",itemsId,orderFulfillmentId);
        orderRepository.deleteItemRetired(itemsId,orderFulfillmentId);

        return true;
    }
}
