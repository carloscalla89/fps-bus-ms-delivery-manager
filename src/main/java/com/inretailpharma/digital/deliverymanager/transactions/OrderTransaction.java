package com.inretailpharma.digital.deliverymanager.transactions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.Client;
import com.inretailpharma.digital.deliverymanager.entity.OrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.OrderStatus;
import com.inretailpharma.digital.deliverymanager.entity.OrderWrapperResponse;
import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrder;
import com.inretailpharma.digital.deliverymanager.entity.ServiceLocalOrderIdentity;
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


    public OrderTransaction(OrderRepositoryService orderRepositoryService,
                            OrderCancellationService orderCancellationService,
                            ObjectToMapper objectToMapper
                            ) {
        this.orderRepositoryService = orderRepositoryService;
        this.orderCancellationService = orderCancellationService;
        this.objectMapper = objectToMapper;
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

        serviceLocalOrderIdentity.setServiceType(
                Optional
                        .ofNullable(orderRepositoryService.getServiceTypeByCode(orderDto.getServiceTypeCode()))
                        .orElse(orderRepositoryService.getServiceTypeByCode(Constant.NOT_DEFINED_SERVICE))

        );
        serviceLocalOrderIdentity.setOrderFulfillment(orderFulfillmentResp);

        // Set status from delivery dispatcher
        OrderStatus orderStatus = getStatusOrderFromDeliveryDispatcher(orderDto);
        serviceLocalOrderIdentity.setOrderStatus(orderStatus);
        // ----------------------------------------------------

        // set ServiceDetail from delivery dispatcher and set store canonical
        ServiceLocalOrder serviceLocalOrder = objectMapper.getFromOrderDto(centerCompanyCanonical, orderDto);
        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);

        serviceLocalOrder.setCancellationCode(Constant.CancellationStockDispatcher.getByName(orderStatus.getType()).getId());
        serviceLocalOrder.setCancellationObservation(Constant.CancellationStockDispatcher.getByName(orderStatus.getType()).getReason());
        serviceLocalOrder.setDateCreated(DateUtils.getLocalDateTimeObjectNow());
        serviceLocalOrder.setDateCancelled(
                Optional.ofNullable(Constant.CancellationStockDispatcher.getByName(orderStatus.getType()).getId())
                        .map(res -> DateUtils.getLocalDateTimeObjectNow())
                        .orElse(null)
        );
        ServiceLocalOrder serviceLocalOrderResponse =  orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);

        // Set values to return wrapped
        OrderWrapperResponse orderWrapperResponse = new OrderWrapperResponse();

        orderWrapperResponse.setFulfillmentId(orderFulfillmentResp.getId());

        orderWrapperResponse.setOrderStatusCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getCode());
        orderWrapperResponse.setOrderStatusName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getType());
        orderWrapperResponse.setOrderStatusDetail(serviceLocalOrderResponse.getStatusDetail());

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

        log.info("[END] createOrderReactive");
        return objectMapper.setsOrderWrapperResponseToOrderCanonical(orderWrapperResponse, orderDto);
    }

    public OrderStatus  getStatusOrderFromDeliveryDispatcher(OrderDto orderDto) {
        OrderStatus orderStatus;

        // set status

        if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CONFIRMED.getCode());

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
                    .orElse(Constant.SUCCESS_CODE).equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)
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
        return orderRepositoryService.getOrderLightByecommercesIds(ecommerceId);
    }

    public List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderFulfillmentId) {
        return orderRepositoryService.getOrderItemByOrderFulfillmentId(orderFulfillmentId);
    }


    public List<IOrderFulfillment> getListOrdersToCancel(String serviceType, String companyCode, Integer maxDayPickup, String statustype) {
        return orderRepositoryService.getListOrdersToCancel(serviceType, companyCode, maxDayPickup, statustype);
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

    public List<CancellationCodeReason> getListCancelReason(String appType) {
        return orderCancellationService.getListCodeCancellationByCode(appType);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updateStatusCancelledOrder(String statusDetail, String cancellationObservation, String cancellationCode,
                                           String cancellationAppType, String orderStatusCode, Long orderFulfillmentId,
                                           LocalDateTime updateLast, LocalDateTime dateCancelled) {
        log.info("[START] updateStatusCancelledOrder transactional - statusDetail:{}, " +
                 "cancellationObservation:{},orderStatusCode:{}, orderFulfillmentId:{}, updateLast:{}, dateCancelled:{}"
                 ,statusDetail, cancellationObservation, orderStatusCode, orderFulfillmentId, updateLast, dateCancelled);

        orderRepositoryService.updateStatusCancelledOrder(statusDetail, cancellationObservation, cancellationCode,
                cancellationAppType, orderStatusCode, orderFulfillmentId, updateLast, dateCancelled);
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
}
