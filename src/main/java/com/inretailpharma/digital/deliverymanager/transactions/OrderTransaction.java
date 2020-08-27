package com.inretailpharma.digital.deliverymanager.transactions;

import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.StoreCenterCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.entity.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderItemFulfillment;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.service.OrderRepositoryService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
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
    public OrderWrapperResponse createOrderTransaction(OrderFulfillment orderFulfillment, OrderDto orderDto,
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
                        .orElse(orderRepositoryService.getServiceTypeByCode(Constant.Constans.NOT_DEFINED_SERVICE))

        );
        serviceLocalOrderIdentity.setOrderFulfillment(orderFulfillmentResp);

        // Set status from delivery dispatcher
        OrderStatus orderStatus = getStatusOrderFromDeliveryDispatcher(orderDto);
        serviceLocalOrderIdentity.setOrderStatus(orderStatus);
        // ----------------------------------------------------

        // Create and set object ServiceLocalOrder
        ServiceLocalOrder serviceLocalOrder = new ServiceLocalOrder();

        serviceLocalOrder.setCenterCode(centerCompanyCanonical.getLocalCode());
        serviceLocalOrder.setCompanyCode(centerCompanyCanonical.getCompanyCode());

        serviceLocalOrder.setServiceLocalOrderIdentity(serviceLocalOrderIdentity);

        // Set attempt of attempt to insink and tracker
        serviceLocalOrder.setAttempt(Constant.Constans.ONE_ATTEMPT);

        if (!(serviceLocalOrderIdentity.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode())
                || serviceLocalOrderIdentity.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.ERROR_RESERVED_ORDER.getCode()))) {
            serviceLocalOrder.setAttemptTracker(Constant.Constans.ONE_ATTEMPT);
        }

        Optional
                .ofNullable(orderDto.getOrderStatusDto())
                .ifPresent(r -> serviceLocalOrder.setStatusDetail(r.getDescription()));
        
        Optional.ofNullable(orderDto.getSchedules())
        		.ifPresent(s -> serviceLocalOrder.setLeadTime(s.getLeadTime()));

        ServiceLocalOrder serviceLocalOrderResponse =  orderRepositoryService.saveServiceLocalOrder(serviceLocalOrder);

        // Set the values of return of transaction as wrapped
        OrderWrapperResponse orderWrapperResponse = new OrderWrapperResponse();

        orderWrapperResponse.setFulfillmentId(orderFulfillmentResp.getId());

        orderWrapperResponse.setOrderStatusCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getCode());
        orderWrapperResponse.setOrderStatusName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getOrderStatus().getType());
        orderWrapperResponse.setOrderStatusDetail(serviceLocalOrderResponse.getStatusDetail());

        orderWrapperResponse.setServiceCode(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getCode());
        orderWrapperResponse.setServiceName(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getName());
        orderWrapperResponse.setServiceType(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getType());
        orderWrapperResponse.setServiceSourcechannel(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getSourceChannel());
        orderWrapperResponse.setServiceEnabled(serviceLocalOrderResponse.getServiceLocalOrderIdentity().getServiceType().getEnabled());

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

        return orderWrapperResponse;
    }


    public OrderStatus  getStatusOrderFromDeliveryDispatcher(OrderDto orderDto) {
        OrderStatus orderStatus;

        // set status

        if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId() != null) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.SUCCESS_FULFILLMENT_PROCESS.getCode());

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
                .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_RELEASE_ORDER.getCode());
        } else if(Optional
                .ofNullable(orderDto.getOrderStatusDto().getCode())
                .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.OrderStatus.ERROR_UPDATE_TRACKER_BILLING.getCode())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_UPDATE_TRACKER_BILLING.getCode());

        } else if (Optional
                    .ofNullable(orderDto.getOrderStatusDto().getCode())
                    .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)
                    && Optional
                        .ofNullable(orderDto.getPayment().getType())
                        .orElse(PaymentMethod.PaymentType.CASH.name())
                        .equalsIgnoreCase(PaymentMethod.PaymentType.ONLINE_PAYMENT.name())) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT.getCode());

        } else if (Optional
                .ofNullable(orderDto.getOrderStatusDto().getCode())
                .orElse(Constant.Constans.SUCCESS_CODE).equalsIgnoreCase(Constant.InsinkErrorCode.CODE_ERROR_STOCK)) {

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.CANCELLED_ORDER.getCode());

        } else if (orderDto.getExternalPurchaseId() != null && orderDto.getTrackerId()==null){

            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode());
        } else {
            orderStatus = orderRepositoryService.getOrderStatusByCode(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.getCode());
        }

        return orderStatus;

    }

    public IOrderFulfillment getOrderByecommerceId(Long ecommerceId) {
        return orderRepositoryService.getOrderByecommerceId(ecommerceId);
    }


    public List<IOrderItemFulfillment> getOrderItemByOrderFulfillmentId(Long orderFulfillmentId) {
        return orderRepositoryService.getOrderItemByOrderFulfillmentId(orderFulfillmentId);
    }

    public List<IOrderFulfillment> getOrdersByStatus(String status){
        return orderRepositoryService.getListOrdersByStatus(new HashSet<>(Collections.singletonList(status)));
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
                                           String cancellationAppType, String orderStatusCode, Long orderFulfillmentId) {
        log.info("[START] updateStatusCancelledOrder transactional - statusDetail:{}, " +
                 "cancellationObservation:{},orderStatusCode:{}, orderFulfillmentId:{}"
                 ,statusDetail, cancellationObservation, orderStatusCode, orderFulfillmentId);

        orderRepositoryService.updateStatusCancelledOrder(statusDetail, cancellationObservation, cancellationCode,
                cancellationAppType, orderStatusCode, orderFulfillmentId);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {Exception.class}, isolation = Isolation.READ_COMMITTED)
    public void updatePartialOrder(String statusDetail, String cancellationObservation, String cancellationCode,
                                           String cancellationAppType, String orderStatusCode, Long orderFulfillmentId) {
        log.info("[START] updateStatusCancelledOrder transactional - statusDetail:{}, " +
                        "cancellationObservation:{},orderStatusCode:{}, orderFulfillmentId:{}"
                ,statusDetail, cancellationObservation, orderStatusCode, orderFulfillmentId);


    }

    public OrderCanonical updatePartialOrder(OrderDto partialOrderDto) {
        IOrderFulfillment iOrderFulfillment = getOrderByecommerceId(partialOrderDto.getEcommercePurchaseId());
        List<IOrderItemFulfillment> iOrderItemFulfillment = getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId());
        List<String> itemsCodeToDelete = getItemsToDelete(partialOrderDto, iOrderItemFulfillment);

        if (!CollectionUtils.isEmpty(itemsCodeToDelete)) {
            log.info("Items to delete : {}", itemsCodeToDelete.toString());
            orderRepositoryService.deleteItemsRetired(itemsCodeToDelete, iOrderItemFulfillment.get(0).getOrderFulfillmentId());
        }
        orderRepositoryService.updatePartialOrderDetail(partialOrderDto, iOrderItemFulfillment);
        orderRepositoryService.updatePartialOrderHeader(partialOrderDto);
        IOrderFulfillment orderUpdated = this.getOrderByecommerceId(partialOrderDto.getEcommercePurchaseId());
        log.info("The order {} was updated sucessfully ", orderUpdated.getOrderId());
        return objectMapper.convertIOrderDtoToOrderFulfillmentCanonical(orderUpdated);


    }

    private List<String> getItemsToDelete(OrderDto partialOrderDto, List<IOrderItemFulfillment> iOrderItemFulfillment) {

        return partialOrderDto.getItemsRetired().stream()
                .filter(order ->
                        iOrderItemFulfillment.stream()
                                .filter(itemFulFillment -> itemFulFillment.getQuantity().equals(order.getQuantity()))
                                .findFirst().orElse(null) != null
                ).map(item -> item.getSku()).collect(Collectors.toList());
    }
}
