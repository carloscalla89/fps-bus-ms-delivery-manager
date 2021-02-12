package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.adapter.AdapterInterface;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderDetailCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
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

import java.time.LocalDateTime;

import java.util.Optional;

@Slf4j
@Component("proxy")
public class OrderFacadeProxyImpl implements OrderFacadeProxy{


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

    @Qualifier("auditadapter")
    @Autowired
    private AdapterInterface adapterAuditInterface;

    @Autowired
    private final ApplicationContext context;

    private OrderFacadeProxyImpl(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public Mono<OrderCanonical> sendOrderToTracker(Long orderId, Long ecommerceId, Long externalId, String serviceTypeCode,
                                                   String statusDetail, String statusName, String orderCancelCode,
                                                   String orderCancelObservation,boolean sendNewAudit) {

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
                ).flatMap(responses -> getOrderResponse(
                                            responses,
                                            orderId,
                                            ecommerceId,
                                            externalId,
                                            orderCancelCode,
                                            orderCancelObservation,
                                            null,
                                            sendNewAudit
                                        )
                );

    }

    @Override
    public Mono<OrderCanonical> sendToUpdateOrder(Long orderId, Long ecommerceId, Long externalId, ActionDto actionDto,
                                                  String serviceType, String serviceTypeCode, String source,
                                                  String companyCode, String localCode, String statusCode,
                                                  boolean sendNewAudit) {

        log.info("sendToUpdateOrder proxy: orderId:{}, ecommerceId:{}, action:{}, sendToUpdateOrder:{}, serviceType:{}, " +
                 "serviceTypeCode:{}", orderId, ecommerceId, actionDto, sendNewAudit, serviceType, serviceTypeCode);

        CancellationCodeReason codeReason;

        UtilClass utilClass = new UtilClass(serviceTypeCode,serviceType, actionDto.getAction(), actionDto.getOrigin(),
                                            statusCode);

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
                                                orderId,
                                                ecommerceId,
                                                externalId,
                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                actionDto.getOrderCancelObservation(),
                                                Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null),
                                                sendNewAudit
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
                                                    orderId,
                                                    ecommerceId,
                                                    externalId,
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getCode).orElse(null),
                                                    actionDto.getOrderCancelObservation(),
                                                    Optional.ofNullable(codeReason).map(CancellationCodeReason::getAppType).orElse(null),
                                                    sendNewAudit
                                                )
                        )
                        .buffer()
                        .filter(finalResponse ->
                                finalResponse
                                        .stream()
                                        .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                        )
                        .flatMap(responses -> {

                            OrderCanonical orderCanonical = new OrderCanonical();
                            orderCanonical.setEcommerceId(ecommerceId);

                            Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(actionDto.getAction()).getOrderStatus();
                            OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                            orderStatusCanonical.setCode(orderStatus.getCode());
                            orderStatusCanonical.setName(orderStatus.name());
                            orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());

                            orderCanonical.setOrderStatus(orderStatusCanonical);

                            return Mono.just(orderCanonical);
                        }).switchIfEmpty(Mono.defer(() -> {
                            OrderCanonical orderCanonical = new OrderCanonical();
                            orderCanonical.setEcommerceId(ecommerceId);

                            Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(actionDto.getAction()).getOrderStatusError();
                            OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                            orderStatusCanonical.setCode(orderStatus.getCode());
                            orderStatusCanonical.setName(orderStatus.name());
                            orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());

                            orderCanonical.setOrderStatus(orderStatusCanonical);

                            return Mono.just(orderCanonical);
                        })).single();
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
                        orderId,
                        ecommerceId,
                        externalId,
                        null,
                        null,
                        null,
                        sendNewAudit
                        )
                )
                .buffer()
                .filter(finalResponse ->
                        finalResponse
                                .stream()
                                .allMatch(fr -> Constant.OrderStatus.getByName(fr.getOrderStatus().getName()).isSuccess())
                )
                .flatMap(responses -> {

                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setEcommerceId(ecommerceId);

                    Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(actionDto.getAction()).getOrderStatus();
                    OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                    orderStatusCanonical.setCode(orderStatus.getCode());
                    orderStatusCanonical.setName(orderStatus.name());
                    orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());

                    orderCanonical.setOrderStatus(orderStatusCanonical);

                    return Mono.just(orderCanonical);
                }).switchIfEmpty(Mono.defer(() -> {
                    OrderCanonical orderCanonical = new OrderCanonical();
                    orderCanonical.setEcommerceId(ecommerceId);

                    Constant.OrderStatus orderStatus = Constant.OrderStatusTracker.getByActionName(actionDto.getAction()).getOrderStatusError();
                    OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
                    orderStatusCanonical.setCode(orderStatus.getCode());
                    orderStatusCanonical.setName(orderStatus.name());
                    orderStatusCanonical.setStatusDate(DateUtils.getLocalDateTimeNow());

                    orderCanonical.setOrderStatus(orderStatusCanonical);

                    return Mono.just(orderCanonical);
                })).single();



    }

    @Override
    public Mono<OrderCanonical> getOrderResponse(OrderCanonical orderCanonical, Long id, Long ecommerceId, Long externalId,
                                                  String orderCancelCode, String orderCancelObservation,
                                                  String orderCancelAppType, boolean sendNewAudit) {

        LocalDateTime localDateTime = DateUtils.getLocalDateTimeObjectNow();

        orderCanonical.setEcommerceId(ecommerceId);

        orderTransaction.updateStatusCancelledOrder(
                orderCanonical.getOrderStatus().getDetail(), orderCancelObservation,
                orderCancelCode, orderCancelAppType,
                orderCanonical.getOrderStatus().getCode(), id,
                localDateTime, Optional.ofNullable(orderCancelCode).map(oc -> localDateTime).orElse(null)
        );

        orderCanonical.setExternalId(externalId);
        orderCanonical.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeWithFormat(localDateTime));

        if (!orderCanonical.getOrderStatus().getCode().equalsIgnoreCase(Constant.OrderStatus.END_STATUS_RESULT.getCode())) {
            adapterAuditInterface.updateExternalAudit(sendNewAudit, orderCanonical).subscribe();
        }

        return Mono.just(orderCanonical);

    }

}
