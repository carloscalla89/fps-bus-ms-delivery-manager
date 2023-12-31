package com.inretailpharma.digital.deliverymanager.facade;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inretailpharma.digital.deliverymanager.adapter.IAuditAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.INotificationAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.IRoutingAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.IStoreAdapter;
import com.inretailpharma.digital.deliverymanager.adapter.ITrackerAdapter;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrdersSelectedResponse;
import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.LiquidationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.CustomException;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.service.OrderInfoService;
import com.inretailpharma.digital.deliverymanager.service.OrderStatusService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
public abstract class FacadeAbstractUtil {

    @Autowired
    private OrderTransaction orderTransaction;

    @Autowired
    private ObjectToMapper objectToMapper;

    @Autowired
    private ApplicationParameterService applicationParameterService;

    @Autowired
    private IStoreAdapter iStoreAdapter;

    @Autowired
    @Qualifier("trackerAdapter")
    private ITrackerAdapter iTrackerAdapter;

    @Autowired
    private IAuditAdapter iAuditAdapter;

    @Autowired
    private LiquidationFacade liquidationFacade;

    @Autowired
    private OrderStatusService orderStatusService;

    private OrderInfoService orderInfoService;
    
    @Autowired
    private IRoutingAdapter iRoutingAdapter;

    protected List<IOrderFulfillment> getListOrdersToCancel(String serviceType, String companyCode, Integer maxDayPickup,
                                                            String statustype) {
        return orderTransaction.getListOrdersToCancel(serviceType, companyCode, maxDayPickup, statustype);
    }

    protected LiquidationCanonical getLiquidationStatusByDigitalStatusCode(String code) {
        return orderTransaction.getLiquidationCanonicalByOrderStatusCode(code);
    }

    protected String getValueOfParameter(String parameter) {
        return applicationParameterService.getApplicationParameterByCodeIs(parameter).getValue();
    }

    protected IOrderFulfillment getOrderLightByecommerceId(Long ecommerceId) {
        return orderTransaction.getOrderLightByecommerceId(ecommerceId);
    }

    protected IOrderFulfillment getOnlyOrderByecommerceId(Long ecommerceId) {
        return orderTransaction.getOnlyOrderStatusByecommerceId(ecommerceId);
    }

    protected boolean existOrder(Long ecommerceId) {
        return Optional
                .ofNullable(orderTransaction.getOnlyOrderStatusByecommerceId(ecommerceId))
                .isPresent();
    }

    protected boolean getOnlyOrderStatusFinalByecommerceId(Long ecommerceId) {
        return Optional
                .ofNullable(orderTransaction.getOnlyOrderStatusByecommerceId(ecommerceId))
                .filter(val -> !Constant.OrderStatus.getFinalStatusByCode(val.getStatusCode()))
                .isPresent();
    }

    protected List<CancellationCanonical> getCancellationsCodeByAppTypeList(List<String> appType, String type) {
        return objectToMapper.convertEntityOrderCancellationToCanonical(orderTransaction.getListCancelReason(appType, type));
    }

    protected Mono<OrderCanonical> getDataToSentAudit(OrderCanonical orderCanonical, ActionDto actionDto) {

        log.info("order:{}, target:{}, action:{}", orderCanonical.getEcommerceId(), orderCanonical.getTarget(),actionDto);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeObjectNow();
        orderCanonical.setUpdateBy(actionDto.getUpdatedBy());
        orderCanonical.setSource(actionDto.getOrigin());
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        orderCanonical.getOrderStatus().setCancellationCode(actionDto.getOrderCancelCode());
        orderCanonical.getOrderStatus().setCancellationObservation(actionDto.getOrderCancelObservation());

        return Mono.just(orderCanonical);

    }

    protected Mono<OrderCanonical> updateOrderInfulfillment(OrderCanonical orderCanonical, Long id, Long ecommerceId,
                                                            Long externalId, String orderCancelCode,
                                                            String orderCancelObservation, String source, String target,
                                                            String updateBy, String actionDate) {

        log.info("Target to send:{}, updateBy:{}",target,updateBy);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);
        orderCanonical.setUpdateBy(updateBy);

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation, orderCancelCode,
                orderCanonical.getOrderStatus().getCode(), id, localDateTime,
                Optional.ofNullable(orderCancelCode).map(oc -> localDateTime).orElse(null)
        );

        orderCanonical.setExternalId(externalId);
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        orderCanonical.getOrderStatus().setCancellationCode(orderCancelCode);
        orderCanonical.getOrderStatus().setCancellationObservation(orderCancelObservation);

        return Mono.just(orderCanonical);

    }

    protected Mono<OrderCanonical> updateOrderInfulfillment(OrderCanonical orderCanonical,Long ecommerceId,
                                                            String source, String target, String updateBy, String actionDate) {

        log.info("Target to send:{}, updateBy:{}",target,updateBy);

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeByInputString(actionDate);

        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setSource(source);
        orderCanonical.setTarget(target);
        orderCanonical.setUpdateBy(updateBy);

        orderTransaction.updateStatusOrder(
                ecommerceId, orderCanonical.getOrderStatus().getCode(), orderCanonical.getOrderStatus().getDetail(),
                localDateTime
        );

        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        return Mono.just(orderCanonical);

    }

    protected OrderCanonical getOrderFromIOrdersProjects(Long ecommerceId) {

        return getOrderFromIOrdersProjects(orderTransaction.getOrderByecommerceId(ecommerceId));

    }

    protected OrderCanonical getOrderToOrderTracker(IOrderFulfillment iOrderFulfillment) {

        return objectToMapper.getOrderToOrderTracker(
                iOrderFulfillment,
                orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId()));
    }

    protected OrderCanonical getOrderFromIOrdersProjects(IOrderFulfillment iOrderFulfillment) {

        return objectToMapper
                .getOrderFromIOrdersProjects(
                        iOrderFulfillment,
                        orderTransaction.getOrderItemByOrderFulfillmentId(iOrderFulfillment.getOrderId())
                );

    }

    protected List<IOrderFulfillment> getOrderLightByecommercesIds(Set<Long> ecommerceIds) {
        return orderTransaction.getOrderLightByecommercesIds(ecommerceIds);
    }

    protected List<IOrderFulfillment> getOrderByEcommercesIds(Set<Long> ecommerceIds) {
        return orderTransaction.getOrderByEcommercesIds(ecommerceIds);
    }

    protected void updateOrderOnlinePaymentStatusByExternalId(Long orderId, String onlinePaymentStatus) {
        orderTransaction.updateOrderOnlinePaymentStatusByExternalId(orderId,onlinePaymentStatus);
    }

    protected Mono<Boolean> processSendNotification(ActionDto actionDto, IOrderFulfillment iOrderFulfillment) {

        INotificationAdapter iNotificationAdapter = (actionDtoParam, iOrderFulfillmentParam) -> Mono.just(Boolean.TRUE);

        return iNotificationAdapter.sendNotification(actionDto,iOrderFulfillment);

    }

    protected boolean getValueBoolenOfParameter(String parameter) {

        return Optional
                .ofNullable(applicationParameterService.getApplicationParameterByCodeIs(parameter))
                .map(val -> Constant.Logical.getByValueString(val.getValue()).value())
                .orElse(false);
    }


    protected Mono<OrderCanonical> createOrderFulfillment(OrderDto orderDto) {

        try {
            log.info("[START] create-order:{}", new ObjectMapper().writeValueAsString(orderDto));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return Mono
                .defer(() -> iStoreAdapter.getStoreByCompanyCodeAndLocalCode(orderDto.getCompanyCode(), orderDto.getLocalCode()))
                .zipWith(
                        Mono.just(existOrder(orderDto.getEcommercePurchaseId()))
                                .filter(val -> !val || orderDto.getSource().equalsIgnoreCase(Constant.SOURCE_AGORA))
                                .switchIfEmpty(
                                        Mono.defer(() ->
                                                Mono.error(new CustomException("Order already exist", HttpStatus.INTERNAL_SERVER_ERROR.value())))),
                            (storeCenter, existOrder) -> {
                            	
                            orderDto.setExternalRouting(storeCenter.isExternalRoutingEnabled());

                            OrderCanonical orderCanonicalResponse = orderTransaction.processOrderTransaction(
                                    objectToMapper.convertOrderdtoToOrderEntity(orderDto),
                                    orderDto,
                                    storeCenter
                            );
                            orderCanonicalResponse.setStoreCenter(storeCenter);

                            iAuditAdapter.createAudit(orderCanonicalResponse, Constant.UPDATED_BY_INIT);

                            return orderCanonicalResponse;
                })
                .flatMap(order -> {
                    log.info("[START] Preparation to send order:{}, companyCode:{}, status:{}, classImplement:{}",
                            order.getEcommerceId(), order.getCompanyCode(), order.getOrderStatus(),
                            order.getOrderDetail().getServiceClassImplement());

                    if (Optional.ofNullable(order.getOrderDetail().getServiceEnabled()).filter(val -> val).isPresent()
                            && Constant.OrderStatus.getByName(order.getOrderStatus().getName()).isSuccess()) {

                        return iTrackerAdapter
                                .evaluateTracker(
                                        Constant.TrackerImplementation
                                                .getClassImplement(order.getOrderDetail().getServiceClassImplement())
                                                .getTrackerImplement(),
                                        ActionDto.builder().action(Constant.ActionOrder.FILL_ORDER.name()).build(),
                                        order.getStoreCenter(),
                                        order.getCompanyCode(),
                                        order.getOrderDetail().getServiceType(),
                                        order.getEcommerceId(),
                                        order.getExternalId(),
                                        Optional.ofNullable(order.getOrderStatus().getName())
                                                .filter(r -> r.equalsIgnoreCase(Constant.OrderStatusTracker.CONFIRMED.name()))
                                                .map(r -> Constant.OrderStatusTracker.CONFIRMED_TRACKER.name())
                                                .orElse(order.getOrderStatus().getName()),
                                        Optional.ofNullable(order.getOrderStatus())
                                                .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                .orElse(null),
                                        Optional.ofNullable(order.getOrderStatus())
                                                .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getReason())
                                                .orElse(null),
                                        null,
                                        order.getOrderStatus().getDetail()

                                )
                                .flatMap(response ->
                                        updateOrderInfulfillment(
                                                response,
                                                order.getId(),
                                                order.getEcommerceId(),
                                                order.getExternalId(),
                                                Optional.ofNullable(order.getOrderStatus())
                                                        .map(os -> Constant.CancellationStockDispatcher.getByName(os.getName()).getId())
                                                        .orElse(null),
                                                null,
                                                order.getSource(),
                                                Constant.TrackerImplementation
                                                        .getClassImplement(order.getOrderDetail().getServiceClassImplement())
                                                        .getTargetName(),
                                                Constant.UPDATED_BY_INIT,
                                                null
                                        )
                                )
                                .flatMap(response -> iAuditAdapter.updateAudit(response, Constant.UPDATED_BY_INIT))
                                .flatMap(response -> liquidationFacade.create(response, order))
                                .flatMap(response -> iRoutingAdapter.createOrder(response))
                                .onErrorResume(e -> {
                                    e.printStackTrace();

                                    log.error("Error sending to tracker the order :{}",order);

                                    // Cuando la orden ha fallado al enviar a los trackers
                                    OrderCanonical orderErrorTracker = new OrderCanonical(
                                            order.getEcommerceId(), Constant.OrderStatus.ERROR_INSERT_TRACKER.getCode(),
                                            Constant.OrderStatus.ERROR_INSERT_TRACKER.name(), e.getMessage()
                                    );

                                    return updateOrderInfulfillment(
                                            orderErrorTracker,
                                            order.getId(),
                                            order.getEcommerceId(),
                                            order.getExternalId(),
                                            null,
                                            null,
                                            order.getSource(),
                                            Constant.TrackerImplementation
                                                    .getClassImplement(order.getOrderDetail().getServiceClassImplement())
                                                    .getTargetName(),
                                            Constant.UPDATED_BY_INIT,
                                            null
                                    ).flatMap(response -> iAuditAdapter.updateAudit(response, Constant.UPDATED_BY_INIT));

                                });

                    }
                    log.info("[END] Preparation to send order:{}", order.getEcommerceId());

                    return liquidationFacade.create(order, order);

                }).switchIfEmpty(Mono.defer(() -> {
                    log.error("Error empty Creating the order:{} with companyCode:{}",
                            orderDto.getEcommercePurchaseId(), orderDto.getCompanyCode());

                    // Cuando la orden ha fallado al insertar al DM, se insertará con lo mínimo para registrarlo en la auditoría
                    OrderCanonical orderStatusCanonical = new OrderCanonical(
                            orderDto.getEcommercePurchaseId(), Constant.DeliveryManagerStatus.ORDER_FAILED.getCode(),
                            Constant.DeliveryManagerStatus.ORDER_FAILED.getStatus(), orderDto.getLocalCode(), orderDto.getCompanyCode(),
                            orderDto.getSource(), orderDto.getServiceTypeCode(), "Error empty Creating the order"
                    );

                    iAuditAdapter.createAudit(orderStatusCanonical, Constant.UPDATED_BY_INIT);

                    return Mono.just(orderStatusCanonical);
                }))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error Creating the order:{} with companyCode:{} in Delivery-manager{}",
                            orderDto.getEcommercePurchaseId(), orderDto.getCompanyCode(), e.getMessage());

                    // Cuando la orden ha fallado al insertar al DM, se insertará con lo mínimo para registrarlo en la auditoría
                    OrderCanonical orderStatusCanonical = new OrderCanonical(
                            orderDto.getEcommercePurchaseId(), Constant.DeliveryManagerStatus.ORDER_FAILED.getCode(),
                            Constant.DeliveryManagerStatus.ORDER_FAILED.getStatus(), orderDto.getLocalCode(), orderDto.getCompanyCode(),
                            orderDto.getSource(), orderDto.getServiceTypeCode(), e.getMessage()
                    );

                    iAuditAdapter.createAudit(orderStatusCanonical, Constant.UPDATED_BY_INIT);

                    return Mono.just(orderStatusCanonical);
                })
                .doOnSuccess(r -> log.info("[END] createOrder facade"));
    }

    public Flux<OrderStatusDto> getAllOrderStatus() {
        return Flux.fromIterable(orderStatusService.getAllOrderStatus());
    }

    public  Mono<OrderInfoConsolidated> getOrderInfoDetail(long ecommerceId){
        return orderInfoService.findOrderInfoClientByEcommerceId(ecommerceId);
    }

    public OrdersSelectedResponse getOrderDetail(FilterOrderDTO filter){
        return orderInfoService.getOrderHeaderDetails(filter);
    }

    protected Mono<String> updateVoucher(Long ecommerceId, boolean voucher){
        return Mono.fromCallable(() -> orderTransaction.updateVoucher(ecommerceId, voucher))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.error("Error updateVoucher - ecommerceId:{} - error:{}", ecommerceId, e.getMessage());
                    return Mono.just(Constant.ERROR_PROCESS);
                });
    }
    
    protected Mono<OrderCanonical> notifyRouting(Long orderId, boolean externalRouting, String serviceType, String action, String origin) {
    	return iRoutingAdapter.cancelOrder(orderId, externalRouting, serviceType, action, origin);
    }
    
    
}
