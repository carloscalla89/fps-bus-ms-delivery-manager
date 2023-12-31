package com.inretailpharma.digital.deliverymanager.service.impl;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalResponse;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderItemDto;
import com.inretailpharma.digital.deliverymanager.dto.PaymentMethodDto;
import com.inretailpharma.digital.deliverymanager.dto.RequestFilterDTO;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.repository.*;
import com.inretailpharma.digital.deliverymanager.repository.custom.CustomQueryOrderInfo;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

    private OrderRepository orderRepository;
    private ServiceTypeRepository serviceTypeRepository;
    private OrderStatusRepository orderStatusRepository;
    private ServiceLocalOrderRepository serviceLocalOrderRepository;
    private ClientRepository clientRepository;
    private CustomQueryOrderInfo customQueryOrderInfo;
    private ObjectToMapper objectMapper;

    public OrderRepositoryServiceImpl(OrderRepository orderRepository,
        ServiceTypeRepository serviceTypeRepository,
        OrderStatusRepository orderStatusRepository,
        ServiceLocalOrderRepository serviceLocalOrderRepository,
        CustomQueryOrderInfo customQueryOrderInfo,
        ObjectToMapper objectMapper,
        ClientRepository clientRepository) {
        this.customQueryOrderInfo = customQueryOrderInfo;
        this.orderRepository = orderRepository;
        this.serviceTypeRepository = serviceTypeRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.serviceLocalOrderRepository = serviceLocalOrderRepository;
        this.clientRepository = clientRepository;
        this.objectMapper = objectMapper;
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
    public IOrderFulfillment getOrderLightByecommerceId(Long ecommerceId) {
        return orderRepository.getOrderLightByecommerceId(ecommerceId).stream().findFirst().orElse(null);
    }

    @Override
    public List<IOrderFulfillment> getOrderLightByecommercesIds(Set<Long> ecommercesIds) {
        log.info("repository:{}",ecommercesIds);
        return orderRepository.getOrderLightByecommercesIds(ecommercesIds);
    }

    @Override
    public List<IOrderFulfillment> getOrdersByEcommerceIds(Set<Long> ecommercesIds) {
        log.info("repository:{}",ecommercesIds);
        return orderRepository.getOrdersByEcommerceIds(ecommercesIds);
    }


    @Override
    public void updateStatusCancelledOrder(String statusDetail, String cancellationObservation, String cancellationCode,
                                           String orderStatusCode, Long orderFulfillmentId, LocalDateTime updateLast,
                                           LocalDateTime dateCancelled) {

        serviceLocalOrderRepository.updateStatusCancelledOrder(statusDetail, cancellationObservation, cancellationCode,
                orderStatusCode, orderFulfillmentId, updateLast, dateCancelled);
    }

    @Override
    public void updateStatusOrder(String statusDetail, String orderStatusCode, Long ecommerceId, LocalDateTime updateLast) {
        serviceLocalOrderRepository.updateStatusOrder(statusDetail, orderStatusCode, ecommerceId, updateLast);
    }

    @Override
    public void updateLiquidationStatusOrder(String liquidationStatus, String liquidationStatusDetail,
                                             Long orderfulfillmentId) {

        orderRepository.updateLiquidationStatusOrder(liquidationStatus, liquidationStatusDetail, orderfulfillmentId);

    }

    @Override
    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    @Override
    public IOrderFulfillment getOnlyOrderStatusByecommerceId(Long ecommerceId) {
        return orderRepository.getOnlyOrderStatusByecommerceId(ecommerceId);
    }

    @Override
	public <T> Optional<IOrderResponseFulfillment> getOrderByOrderNumber(Long orderNumber) {
		log.info("CALL Repository--getOrderByOrderNumber:"+orderNumber);
		return orderRepository.getOrderByOrderNumber(orderNumber);
	}

    @Override
    public Mono<OrderCanonicalResponse> getOrder(RequestFilterDTO filter) {
        return customQueryOrderInfo.getOrderInfo(filter);
    }

    @Override
    public boolean updatePartialOrderHeader(OrderDto orderDto) {
        BigDecimal totalCost = orderDto.getTotalCost();
        BigDecimal bigDecimal = orderDto.getDeliveryCost();
        LocalDateTime dateLastUpdated =  DateUtils.getLocalDateTimeObjectNow();
        Long externalPurchaseId = orderDto.getEcommercePurchaseId();

         orderRepository.updatePartialOrder(totalCost,bigDecimal,dateLastUpdated,externalPurchaseId,true,
                 orderDto.getDiscountApplied(),orderDto.getSubTotalCost(),orderDto.getTotalCostNoDiscount(),
                 orderDto.getDiscountAppliedNoDP(),orderDto.getSubTotalWithNoSpecificPaymentMethod(),
                 orderDto.getTotalWithNoSpecificPaymentMethod(),orderDto.getTotalWithPaymentMethod());
         log.info("The order {} header was updated sucessfully",externalPurchaseId);
         return true;
    }

    @Override
    public boolean updatePartialOrderDetail(OrderDto orderDto, List<IOrderItemFulfillment> iOrderItemFulfillment) {

        for (IOrderItemFulfillment itemOriginal : iOrderItemFulfillment) {
            OrderItemDto itemDto = orderDto.getOrderItem().stream().filter(dto-> !dto.isRemoved()) .filter(dto -> dto.getProductCode()
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
                    Integer quantityPresentation = itemDto.getQuantity();
                    BigDecimal unitPrice = itemDto.getUnitPrice();
                    BigDecimal totalPrice = itemDto.getTotalPrice();
                    Integer presentationID = itemDto.getPresentationId();
                    Integer quantityUnits= itemDto.getQuantityUnits();

                    Constant.Logical fractionated = Constant.Logical.parse(itemDto.getFractionated());
                    String presentationDescription = itemDto.getPresentationDescription();
                    orderRepository.updateItemsPartialOrder(quantity, quantityPresentation,unitPrice, totalPrice, fractionated.name(),
                            orderFulfillmentId,quantityUnits, productCode,presentationDescription,presentationID,
                            itemDto.getFractionalDiscount(),itemDto.getPriceList(),itemDto.getPriceAllPaymentMethod(),
                            itemDto.getPriceWithpaymentMethod(),itemDto.getTotalPriceList(),itemDto.getTotalPriceAllPaymentMethod(),
                            itemDto.getTotalPriceWithpaymentMethod(),itemDto.getPromotionalDiscount());
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

    @Override
    public void updatePaymentMethod(OrderDto partialOrderDto, Long orderFulfillmentId) {
        PaymentMethodDto paymentMethod = partialOrderDto.getPayment();
        BigDecimal paidAmount = paymentMethod.getPaidAmount();
        BigDecimal changeAmount = paymentMethod.getChangeAmount();
        Long orderId = partialOrderDto.getEcommercePurchaseId();
        orderRepository.updatePaymentMethod(paidAmount,changeAmount,"Parcial",orderFulfillmentId);
        log.info("PaymentMethod updated succesfully");
    }

    @Override
    public void updateOnlinePaymentStatusByOrderId(Long orderId, String onlinePaymentStatus) {
        orderRepository.updateOnlinePaymentStatusByOrderId(orderId, onlinePaymentStatus);
    }

    @Override
    public void updateVoucherByEcommerceId(Long ecommerceId, boolean voucher) {
        orderRepository.updateVoucherByEcommerceId(ecommerceId, voucher);
    }

    @Override
    public void updateOrderPickupByEcommerceId(OrderDto orderDto) {
        Long ecommerceId = orderDto.getEcommercePurchaseId();
        Integer idPickUp = orderDto.getIdPickUp();
        String dniPickUP = orderDto.getDniPickUp();
        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(orderDto.getDatePickUp());
        orderRepository.updateOrderPickupByEcommerceId(ecommerceId,idPickUp, dniPickUP, localDateTime);
    }
}
