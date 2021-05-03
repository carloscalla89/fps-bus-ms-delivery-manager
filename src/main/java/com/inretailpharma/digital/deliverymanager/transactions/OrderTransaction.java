package com.inretailpharma.digital.deliverymanager.transactions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;

import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import com.inretailpharma.digital.deliverymanager.util.UtilFunctions;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;

import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderResponseFulfillment;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import com.inretailpharma.digital.deliverymanager.util.Constant;

import lombok.extern.slf4j.Slf4j;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;

@Slf4j
@Transactional(propagation = Propagation.REQUIRED, readOnly = true, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
@Component
public class OrderTransaction {

    private OrderRepositoryService orderRepositoryService;
    private OrderCancellationService orderCancellationService;
    private ObjectToMapper objectMapper;
    private ApplicationParameterService applicationParameterService;

    public OrderTransaction(OrderRepositoryService orderRepositoryService,
                            OrderCancellationService orderCancellationService,
                            ObjectToMapper objectToMapper,
                            ApplicationParameterService applicationParameterService
                            ) {
        this.orderRepositoryService = orderRepositoryService;
        this.orderCancellationService = orderCancellationService;
        this.objectMapper = objectToMapper;
        this.applicationParameterService = applicationParameterService;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public OrderCanonical processOrderTransaction(OrderFulfillment orderFulfillment, OrderDto orderDto,
                                                  StoreCenterCanonical centerCompanyCanonical) {

        log.info("[START ] createOrderReactive");

        Client client = orderRepositoryService.saveClient(orderFulfillment.getClient());

        orderFulfillment.setClient(client);

        OrderFulfillment orderFulfillmentResp = orderRepositoryService.createOrder(orderFulfillment);

        // Set Object ServiceLocalOrderIdentity
        ServiceLocalOrderIdentity serviceLocalOrderIdentity = new ServiceLocalOrderIdentity();

        ServiceType serviceType = Optional
                                    .ofNullable(orderRepositoryService.getServiceTypeByCode(orderDto.getServiceTypeCode()))
                                    .orElse(orderRepositoryService.getServiceTypeByCode(Constant.NOT_DEFINED_SERVICE));

        serviceLocalOrderIdentity.setServiceType(serviceType);
        serviceLocalOrderIdentity.setOrderFulfillment(orderFulfillmentResp);

        // Set status from delivery dispatcher
        OrderStatus orderStatus = getStatusOrderFromDeliveryDispatcher(orderDto);
        serviceLocalOrderIdentity.setOrderStatus(orderStatus);
        // ----------------------------------------------------

        // get days to pickup from application table DB
        String dayToPickup = getApplicationParameter(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET);
        //

        // set ServiceDetail from delivery dispatcher and set store canonical
        ServiceLocalOrder serviceLocalOrder = objectMapper
                                                .getFromOrderDto(centerCompanyCanonical, orderDto, serviceType, dayToPickup);

        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);
        serviceLocalOrder.setLeadTime(
                Optional.ofNullable(orderDto.getSchedules().getLeadTime())
                        .filter(val -> val > 0)
                        .orElseGet(() -> Optional
                                            .ofNullable(getApplicationParameter(Constant.ApplicationsParameters.DEFAULT_INTERVAL_TIME_BY_SERVICE_
                                                    + serviceType.getShortCode())
                                            )
                                            .map(Integer::parseInt)
                                            .orElse(0)
                        )
        );
        serviceLocalOrder.setCancellationCode(Constant.CancellationStockDispatcher.getByName(orderStatus.getType()).getId());
        serviceLocalOrder.setCancellationObservation(null);
        serviceLocalOrder.setStatusDetail(Optional.ofNullable(orderDto.getOrderStatusDto()).map(OrderStatusDto::getDescription).orElse(null));
        serviceLocalOrder.setDateCreated(DateUtils.getLocalDateTimeObjectNow());
        serviceLocalOrder.setDateCancelled(
                Optional.ofNullable(Constant.CancellationStockDispatcher.getByName(orderStatus.getType()).getId())
                        .map(res -> DateUtils.getLocalDateTimeObjectNow())
                        .orElse(null)
        );

        serviceLocalOrder.setLiquidationStatus(UtilFunctions.processLiquidationStatus.process(orderStatus.getType()));

        ServiceLocalOrder serviceLocalOrderResponse =  orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);

        // Set values to return wrapped
        OrderWrapperResponse orderWrapperResponse = new OrderWrapperResponse();

        orderWrapperResponse.setFulfillmentId(orderFulfillmentResp.getId());

        orderWrapperResponse.setOrderStatusCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getCode());
        orderWrapperResponse.setOrderStatusName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getType());
        orderWrapperResponse.setOrderStatusDetail(serviceLocalOrderResponse.getStatusDetail());
        orderWrapperResponse.setCancellationCode(Constant.CancellationStockDispatcher.getByName(orderStatus.getType()).getId());
        orderWrapperResponse.setCancellationDescription(Constant.CancellationStockDispatcher.getByName(orderStatus.getType()).getReason());
        orderWrapperResponse.setServiceCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getCode());
        orderWrapperResponse.setServiceShortCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getShortCode());
        orderWrapperResponse.setServiceName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getName());
        orderWrapperResponse.setServiceType(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getType());
        orderWrapperResponse.setServiceSourcechannel(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getSourceChannel());

        orderWrapperResponse.setServiceSendNewFlowEnabled(
                serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().isSendNewFlowEnabled()
        );
        orderWrapperResponse.setServiceSendNotificationEnabled(
                serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().isSendNotificationEnabled()
        );

        orderWrapperResponse.setServiceEnabled(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getEnabled());
        orderWrapperResponse.setServiceClassImplement(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getClassImplement());

        orderWrapperResponse.setAttemptBilling(serviceLocalOrderResponse.getAttempt());
        orderWrapperResponse.setAttemptTracker(serviceLocalOrderResponse.getAttemptTracker());

        orderWrapperResponse.setCompanyCode(centerCompanyCanonical.getCompanyCode());
        orderWrapperResponse.setLocalName(centerCompanyCanonical.getName());
        orderWrapperResponse.setLocalCode(centerCompanyCanonical.getLocalCode());
        orderWrapperResponse.setLocalAddress(centerCompanyCanonical.getAddress());
        orderWrapperResponse.setLocalDescription(centerCompanyCanonical.getDescription());
        orderWrapperResponse.setLocalId(centerCompanyCanonical.getLegacyId());
        orderWrapperResponse.setLocalLatitude(centerCompanyCanonical.getLatitude());
        orderWrapperResponse.setLocalLongitude(centerCompanyCanonical.getLongitude());

        orderWrapperResponse.setStartHour(serviceLocalOrderResponse.getStartHour());
        orderWrapperResponse.setEndHour(serviceLocalOrderResponse.getEndHour());
        orderWrapperResponse.setLeadTime(serviceLocalOrderResponse.getLeadTime());
        orderWrapperResponse.setDaysToPickup(serviceLocalOrderResponse.getDaysToPickup());
        orderWrapperResponse.setLiquidationStatus(serviceLocalOrderResponse.getLiquidationStatus());

        log.info("[END] createOrderReactive");
        return objectMapper.setsOrderWrapperResponseToOrderCanonical(orderWrapperResponse, orderDto);
    }

    public OrderStatus  getStatusOrderFromDeliveryDispatcher(OrderDto orderDto) {
        OrderStatus orderStatus;

        // set status

        if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CONFIRMED.getCode());

        } else if (Optional
                    .ofNullable(orderDto.getOrderStatusDto().getCode())
                    .orElse(Constant.SUCCESS_CODE)
                    .equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)

                    && Optional
                        .ofNullable(orderDto.getPayment().getType())
                        .orElse(PaymentMethod.PaymentType.CASH.name())
                        .equalsIgnoreCase(PaymentMethod.PaymentType.ONLINE_PAYMENT.name())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK.getCode());
            orderStatus.setType(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK.name());

        } else if (Optional
                    .ofNullable(orderDto.getOrderStatusDto().getCode())
                    .orElse(Constant.SUCCESS_CODE).equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CANCELLED_ORDER_NOT_ENOUGH_STOCK.getCode());
            orderStatus.setType(Constant.OrderStatus.CANCELLED_ORDER_NOT_ENOUGH_STOCK.name());

        } else if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId()==null){

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());

        } else if (Optional
                .ofNullable(orderDto.getOrderStatusDto().getCode())
                .orElse(Constant.SUCCESS_CODE).equalsIgnoreCase(Constant.DeliveryManagerStatus.ORDER_FAILED.name())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ORDER_FAILED.getCode());

        } else {
            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        }

        return orderStatus;

    }

    public IOrderFulfillment getOrderByecommerceId(Long ecommerceId) {
        return orderRepositoryService.getOrderByecommerceId(ecommerceId);
    }

    public IOrderFulfillment getOrderLightByecommerceId(Long ecommerceId) {
        return orderRepositoryService.getOrderLightByecommerceId(ecommerceId);
    }

    public List<IOrderFulfillment> getOrderLightByecommercesIds(Set<Long> ecommerceId) {
        log.info("getTransaction:{}",ecommerceId);
        return orderRepositoryService.getOrderLightByecommercesIds(ecommerceId);
    }

    public List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderFulfillmentId) {
        return orderRepositoryService.getOrderItemByOrderFulfillmentId(orderFulfillmentId);
    }


    public List<IOrderFulfillment> getListOrdersToCancel(String serviceType, String companyCode, Integer maxDayPickup, String statustype) {
        return orderRepositoryService.getListOrdersToCancel(serviceType, companyCode, maxDayPickup, statustype);
    }


    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateStatusOrder(Long ecommerceId, String orderStatusCode, String statusDetail, LocalDateTime updateLast) {
        log.info("[START] updateOrderStatus - ecommerceId:{} - orderStatusCode:{}, statusDetail:{}, updateLast:{}",
                ecommerceId, orderStatusCode, statusDetail, updateLast);

        orderRepositoryService.updateStatusOrder(statusDetail, orderStatusCode, ecommerceId, updateLast);
    }

    public List<CancellationCodeReason> getListCancelReason(String appType) {
        return orderCancellationService.getListCodeCancellationByCode(appType);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateStatusCancelledOrder(String statusDetail, String cancellationObservation, String cancellationCode,
                                           String orderStatusCode, Long orderFulfillmentId, LocalDateTime updateLast,
                                           LocalDateTime dateCancelled) {
        log.info("[START] updateStatusCancelledOrder transactional - statusDetail:{}, " +
                 "cancellationObservation:{},orderStatusCode:{}, orderFulfillmentId:{}, updateLast:{}, dateCancelled:{}"
                 ,statusDetail, cancellationObservation, orderStatusCode, orderFulfillmentId, updateLast, dateCancelled);

        orderRepositoryService.updateStatusCancelledOrder(statusDetail, cancellationObservation, cancellationCode,
                orderStatusCode, orderFulfillmentId, updateLast, dateCancelled);
    }

    public <T> Optional<IOrderResponseFulfillment> getOrderByOrderNumber(Long orderNumber) {
        return orderRepositoryService.getOrderByOrderNumber(orderNumber);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public OrderCanonical updatePartialOrder(OrderDto partialOrderDto) {
        IOrderFulfillment iOrderFulfillment = getOrderByecommerceId(partialOrderDto.getEcommercePurchaseId());
        List<IOrderItemFulfillment> iOrderItemFulfillment = getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId());
        orderRepositoryService.updatePartialOrderDetail(partialOrderDto, iOrderItemFulfillment);
        orderRepositoryService.updatePartialOrderHeader(partialOrderDto);
        orderRepositoryService.updatePaymentMethod(partialOrderDto,iOrderItemFulfillment.get(0).getOrderFulfillmentId());
        IOrderFulfillment orderUpdated = this.getOrderByecommerceId(partialOrderDto.getEcommercePurchaseId());
        log.info("The order {} was updated sucessfully ", orderUpdated.getOrderId());
        return objectMapper.convertIOrderDtoToOrderFulfillmentCanonical(orderUpdated);

    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateOrderOnlinePaymentStatusByExternalId(Long orderId, String onlinePaymentStatus) {
        orderRepositoryService.updateOnlinePaymentStatusByOrderId(orderId, onlinePaymentStatus);
    }

    private String getApplicationParameter(String code) {
        return applicationParameterService
                .getApplicationParameterByCodeIs(code).getValue();
    }
}
