package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.adapter.AdapterInterface;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderDetailCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.controversies.ControversyRequestDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.service.CenterCompanyService;
import com.inretailpharma.digital.deliverymanager.service.OrderCancellationService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import com.inretailpharma.digital.deliverymanager.util.UtilClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Slf4j
@Component("proxy")
public class OrderFacadeProxyImpl implements OrderFacadeProxy{



    @Qualifier("inkatracker")
    @Autowired
    private OrderExternalService externalServiceInkatracker;

    @Autowired
    private CenterCompanyService centerCompanyService;

    @Qualifier("sellerCenterService")
    @Autowired
    private OrderExternalService sellerCenterService;

    @Autowired
    private OrderCancellationService orderCancellationService;

    @Qualifier("audit")
    @Autowired
    private OrderExternalService orderExternalServiceAudit;

    @Autowired
    private OrderTransaction orderTransaction;

    @Qualifier("trackeradapter")
    @Autowired
    private AdapterInterface adapterTrackerInterface;

    @Autowired
    private final ApplicationContext context;

    private OrderFacadeProxyImpl(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Mono<OrderCanonical> sendOrderToTracker(Long ecommerceId, Long externalId, String serviceTypeCode,
                                                   String statusDetail, String statusName, String orderCancelCode,
                                                   String orderCancelObservation) {

        UtilClass utilClass = new UtilClass(serviceTypeCode);

        return adapterTrackerInterface
                .sendOrderTracker(
                        ((OrderExternalService)context.getBean(utilClass.getClassToTracker())),
                        ecommerceId,
                        externalId,
                        statusDetail,
                        statusName,
                        orderCancelCode,
                        orderCancelObservation,
                        null
                );

    }

    @Override
    public Mono<OrderCanonical> sendToUpdateOrder(Long orderId, Long ecommerceId, Long externalId, ActionDto actionDto,
                                                  String serviceType, String serviceTypeCode, String source,
                                                  String companyCode, String localCode, String statusCode,
                                                  boolean sendToUpdateOrder) {

        // Bloque para enviar al order-tracker

        CancellationCodeReason codeReason;
        if (sendToUpdateOrder) {

            UtilClass utilClass = new UtilClass(serviceTypeCode,serviceType, actionDto.getAction(),
                    actionDto.getOrigin(), statusCode);

            if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {


                if (source.equalsIgnoreCase(Constant.Source.SC.name())) {
                    OrderExternalService sellerCenterService = (OrderExternalService) context.getBean(
                            Constant.SellerCenter.BEAN_SERVICE_NAME
                    );

                    log.info("[START] Add controversy because of order cancellation comming from seller center");

                    ControversyRequestDto controversyRequestDto = new ControversyRequestDto();
                    controversyRequestDto.setDate(DateUtils.getLocalDateTimeNowStr());
                    controversyRequestDto.setText(actionDto.getOrderCancelObservation() != null ? actionDto.getOrderCancelObservation() : "");
                    controversyRequestDto.setType(Constant.SellerCenter.ControversyTypes.CT.getType());

                    sellerCenterService.addControversy(controversyRequestDto, ecommerceId).subscribe();

                    log.info("[END] add controversy");
                }



                if (actionDto.getOrderCancelCode() != null && actionDto.getOrderCancelAppType() != null) {
                    codeReason = orderCancellationService
                            .geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType());
                } else {
                    codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
                }

                // Esta condición está para saber si una orden se manda a cancelar pero no está en el tracker porque de seguro falló al momento de registrarse
                if (Constant.OrderStatus.getByCode(statusCode).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name())
                        || Constant.OrderStatus.getByCode(statusCode).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())) {



                    return adapterTrackerInterface
                                    .sendOrderTracker(
                                            ((OrderExternalService)context.getBean(utilClass.getClassToTracker())),
                                            ecommerceId,
                                            externalId,
                                            null,
                                            Constant.OrderStatus.CANCELLED_ORDER.name(),
                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                            actionDto.getOrderCancelObservation(),
                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null)
                                    )
                                    .flatMap(responses -> getOrderResponse(
                                            responses,
                                            externalId,
                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                            actionDto.getOrderCancelObservation(),
                                            Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null)
                                            )
                                    );

                } else {
                    return Flux
                            .fromIterable(utilClass.getClassesToSend())
                            .flatMap(objectClass ->
                                    ((AdapterInterface)context
                                            .getBean(objectClass))
                                            .getResultfromExternalServices(
                                                    ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                                    ecommerceId,
                                                    actionDto,
                                                    companyCode,
                                                    serviceType,
                                                    orderId,
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                    actionDto.getOrderCancelObservation(),
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null)
                                            )
                            )
                            .flatMap(responses -> getOrderResponse(
                                                    responses,
                                                    externalId,
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                    actionDto.getOrderCancelObservation(),
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null)
                                                  )
                            )
                            .buffer()
                            .flatMap(responses -> {
                                OrderCanonical orderCanonical = new OrderCanonical();

                                return Mono.just(orderCanonical);
                            }).single();
                }

            }


            return Flux
                    .fromIterable(utilClass.getClassesToSend())
                    .flatMap(objectClass ->
                                    ((AdapterInterface)context
                                                        .getBean(objectClass))
                                                        .getResultfromExternalServices(
                                                            ((OrderExternalService)context.getBean(utilClass.getClassImplementationToOrderExternalService(objectClass))),
                                                            ecommerceId,
                                                            actionDto,
                                                            companyCode,
                                                            serviceType,
                                                            orderId,
                                                            null,
                                                            null,
                                                            null
                                                        )
                    )
                    .flatMap(responses -> getOrderResponse(
                            responses,
                            externalId,
                            null,
                            null,
                            null
                            )
                    )
                    .buffer()
                    .flatMap(responses -> {
                        OrderCanonical orderCanonical = new OrderCanonical();

                        return Mono.just(orderCanonical);
                    }).single();





        } else {

            // call the service inkatracker-lite or inkatracker to update the order status (CANCEL, READY_FOR_PICKUP, DELIVERED)

            if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {
                log.info("[START] REQUEST-CANCEL-ORDER for orderId = {}", ecommerceId);

                if (source.equalsIgnoreCase(Constant.Source.SC.name())) {

                    log.info("[START] Add controversy because of order cancellation comming from seller center");

                    ControversyRequestDto controversyRequestDto = new ControversyRequestDto();
                    controversyRequestDto.setDate(DateUtils.getLocalDateTimeNowStr());
                    controversyRequestDto.setText(actionDto.getOrderCancelObservation() != null ? actionDto.getOrderCancelObservation() : "");
                    controversyRequestDto.setType(Constant.SellerCenter.ControversyTypes.CT.getType());

                    sellerCenterService.addControversy(controversyRequestDto, ecommerceId).subscribe();

                    log.info("[END] add controversy");
                }


                if (actionDto.getOrderCancelCode() != null && actionDto.getOrderCancelAppType() != null) {
                    codeReason = orderCancellationService.geByCodeAndAppType(actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType());
                } else {
                    codeReason = orderCancellationService.geByCode(actionDto.getOrderCancelCode());
                }

                Optional.ofNullable(codeReason)
                        .ifPresent(r -> {
                            actionDto.setOrderCancelAppType(r.getAppType());
                            actionDto.setOrderCancelReason(r.getReason());
                            actionDto.setOrderCancelClientReason(r.getClientReason());
                        });


                // Esta condición está para saber si una orden se manda a cancelar pero no está en el tracker porque de seguro falló al momento de registrarse
                if (Constant.OrderStatus.getByCode(statusCode).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_INKAVENTA.name())
                        || Constant.OrderStatus.getByCode(statusCode).name().equalsIgnoreCase(Constant.OrderStatus.ERROR_INSERT_TRACKER.name())) {


                    IOrderFulfillment iOrderFulfillmentCase4 = orderTransaction.getOrderByecommerceId(ecommerceId);

                    return centerCompanyService
                            .getExternalInfo(companyCode, localCode)
                            .flatMap(storeCenterCanonical -> {
                                // creando la orden a tracker CON EL ESTADO CANCELLED

                                return externalServiceInkatracker
                                        .sendOrderToTracker(
                                                iOrderFulfillmentCase4,
                                                orderTransaction.getOrderItemByOrderFulfillmentId(ecommerceId),
                                                storeCenterCanonical,
                                                ecommerceId,
                                                null,
                                                Constant.OrderStatus.CANCELLED_ORDER.name(),
                                                actionDto.getOrderCancelCode(),
                                                actionDto.getOrderCancelObservation()
                                        )
                                        .flatMap(s -> {
                                            OrderCanonical orderCanonical = processTransaction(iOrderFulfillmentCase4, s);
                                            return Mono.just(orderCanonical);
                                        });
                            })
                            .map(r -> {

                                log.info("Action to cancel order");
                                orderTransaction.updateStatusCancelledOrder(
                                        r.getOrderStatus().getDetail(), actionDto.getOrderCancelObservation(),
                                        actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType(),
                                        r.getOrderStatus().getCode(), iOrderFulfillmentCase4.getOrderId()
                                );

                                r.setEcommerceId(ecommerceId);
                                r.setExternalId(iOrderFulfillmentCase4.getExternalId());
                                r.setTrackerId(iOrderFulfillmentCase4.getTrackerId());
                                r.setPurchaseId(Optional.ofNullable(iOrderFulfillmentCase4.getPurchaseId()).map(Integer::longValue).orElse(null));
                                r.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

                                if (!r.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
                                    orderExternalServiceAudit.updateOrderReactive(r).subscribe();
                                }
                                log.info("[END] to update order");

                                return r;
                            });

                }

            }

            actionDto.setExternalBillingId(Optional.ofNullable(ecommerceId).map(Object::toString).orElse(null));

            return externalServiceInkatracker
                    .getResultfromExternalServices(ecommerceId, actionDto, companyCode,
                            serviceType)
                    .map(r -> {

                        log.info("[START] to update order");

                        if (Constant.ActionOrder.CANCEL_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {
                            log.info("Action to cancel order");
                            orderTransaction.updateStatusCancelledOrder(
                                    r.getOrderStatus().getDetail(), actionDto.getOrderCancelObservation(),
                                    actionDto.getOrderCancelCode(), actionDto.getOrderCancelAppType(),
                                    r.getOrderStatus().getCode(), orderId
                            );
                        } else {

                            if (!r.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
                                orderTransaction.updateStatusOrder(orderId, r.getOrderStatus().getCode(),
                                        r.getOrderStatus().getDetail());
                            }


                        }

                        log.info("[END] to update order");

                        r.setEcommerceId(ecommerceId);
                        r.setExternalId(ecommerceId);
                        r.setTrackerId(ecommerceId);
                        r.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

                        if (!r.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
                            orderExternalServiceAudit.updateOrderReactive(r).subscribe();
                        }

                        return r;
                    });

        }

    }

    private Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long externalId, String orderCancelCode,
                                                  String orderCancelObservation, String orderCancelAppType) {

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation,
                orderCancelCode, orderCancelAppType,
                orderCanonical.getOrderStatus().getCode(), orderCanonical.getId()
        );

        orderCanonical.setExternalId(externalId);
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

        if (!orderCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
            orderExternalServiceAudit.updateOrderReactive(orderCanonical).subscribe();
        }
        log.info("[END] to update order");

        return Mono.just(orderCanonical);

    }

    @Override
    public OrderCanonical processTransaction(IOrderFulfillment iOrderFulfillment, OrderCanonical r) {

        Integer attemptTracker = Optional.ofNullable(iOrderFulfillment.getAttemptTracker()).map(n -> n + 1).orElse(null);
        Integer attempt = Optional.ofNullable(iOrderFulfillment.getAttempt()).map(n -> n + 1).orElse(null);

        r.setExternalId(Optional.ofNullable(iOrderFulfillment.getExternalId())
                .orElse(r.getExternalId())
        );

        r.setTrackerId(Optional.ofNullable(iOrderFulfillment.getTrackerId())
                .orElse(r.getTrackerId())
        );

        r.setPurchaseId(Optional.ofNullable(iOrderFulfillment.getPurchaseId()).map(Integer::longValue).orElse(null));

        if (!r.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
            orderTransaction.updateOrderRetrying(
                    iOrderFulfillment.getOrderId(), attempt, attemptTracker,
                    r.getOrderStatus().getCode(), r.getOrderStatus().getDetail(),
                    r.getExternalId(), r.getTrackerId()
            );
        }

        OrderDetailCanonical orderDetail = new OrderDetailCanonical();
        orderDetail.setAttempt(attempt);
        orderDetail.setAttemptTracker(attemptTracker);

        r.setOrderDetail(orderDetail);

        r.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

        if (!r.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
            orderExternalServiceAudit.updateOrderReactive(r).subscribe();
        }

        return r;
    }

}
