package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancelledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ShoppingCartStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.adapter.AdapterInterface;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.entity.ApplicationParameter;
import com.inretailpharma.digital.deliverymanager.mapper.ObjectToMapper;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.service.ApplicationParameterService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class CancellationFacade {

    private AdapterInterface auditAdapter;
    private ApplicationParameterService applicationParameterService;
    private OrderTransaction orderTransaction;
    private ObjectToMapper objectToMapper;
    private final ApplicationContext context;
    private ExternalServicesProperties externalServicesProperties;

    public CancellationFacade(@Qualifier("auditadapter") AdapterInterface auditAdapter,
                              ApplicationParameterService applicationParameterService,
                              OrderTransaction orderTransaction, ObjectToMapper objectToMapper,
                              ApplicationContext context,  ExternalServicesProperties externalServicesProperties) {

        this.auditAdapter = auditAdapter;
        this.applicationParameterService = applicationParameterService;
        this.orderTransaction = orderTransaction;
        this.objectToMapper = objectToMapper;
        this.context = context;
        this.externalServicesProperties = externalServicesProperties;
    }

    public Flux<CancellationCanonical> getOrderCancellationList(String appType) {
        return Flux.fromIterable(
                objectToMapper.convertEntityOrderCancellationToCanonical(orderTransaction.getListCancelReason(appType))
        );
    }

    public Flux<OrderCancelledCanonical> cancelOrderProcess(CancellationDto cancellationDto) {
        log.info("[START] cancelOrderProcess");

        ApplicationParameter daysValue = applicationParameterService
                .getApplicationParameterByCodeIs(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET);

        return Flux
                .fromIterable(orderTransaction
                        .getListOrdersToCancel(
                                cancellationDto.getServiceType(), cancellationDto.getCompanyCode(),
                                Integer.parseInt(daysValue.getValue()), cancellationDto.getStatusType()
                        )
                )
                .parallel()
                .runOn(Schedulers.elastic())
                .flatMap(r -> {

                    log.info("order info- companyCode:{}, centerCode:{}, ecommerceId:{}, serviceTypeCode:{}, getSendNewFlow:{} ",
                            r.getCompanyCode(), r.getCenterCode(), r.getEcommerceId(), r.getServiceTypeCode(), r.getSendNewFlow());

                    ActionDto actionDto = new ActionDto();
                    actionDto.setAction(Constant.ActionOrder.CANCEL_ORDER.name());
                    actionDto.setOrderCancelCode(cancellationDto.getCancellationCode());
                    actionDto.setOrderCancelObservation(cancellationDto.getObservation());

                    OrderExternalService orderExternalService = (OrderExternalService)context.getBean(r.getClassImplement());

                    return orderExternalService
                            .getResultfromExternalServices(r.getEcommerceId(), actionDto, cancellationDto.getCompanyCode(),
                                    cancellationDto.getServiceType(), null)
                            .map(s -> {
                                log.info("[START] Processing the updating of cancelled order, serviceTypeCode:{}, localCode:{}," +
                                                "companyCode:{},statusCode:{}, statusName:{}, ecommerceId:{}",r.getServiceTypeCode(), r.getCenterCode(),
                                        cancellationDto.getCompanyCode(), s.getOrderStatus().getCode(), s.getOrderStatus().getName(),
                                        r.getEcommerceId());


                                orderTransaction.updateStatusCancelledOrder(
                                        s.getOrderStatus().getDetail(), actionDto.getOrderCancelObservation(),
                                        actionDto.getOrderCancelCode(), s.getOrderStatus().getCode(), r.getOrderId(),
                                        DateUtils.getLocalDateTimeObjectNow(), DateUtils.getLocalDateTimeObjectNow()
                                );
                                log.info("[END] Processing the updating of cancelled order");
                                return s;
                            }).defaultIfEmpty(
                                    new OrderCanonical(
                                            r.getEcommerceId(),
                                            Constant.OrderStatus.ERROR_CANCELLED.getCode(),
                                            Constant.OrderStatus.ERROR_CANCELLED.name()
                                    )
                            )
                            .filter(s -> s.getOrderStatus() != null)
                            .map(s -> {
                                s.setEcommerceId(r.getEcommerceId());
                                s.setExternalId(r.getExternalId());
                                s.setTrackerId(r.getTrackerId());
                                s.getOrderStatus().setStatusDate(DateUtils.getLocalDateTimeNow());

                                OrderCancelledCanonical orderCancelledCanonical = new OrderCancelledCanonical();
                                orderCancelledCanonical.setEcommerceId(r.getEcommerceId());
                                orderCancelledCanonical.setExternalId(r.getExternalId());
                                orderCancelledCanonical.setCompany(r.getCompanyCode());
                                orderCancelledCanonical.setLocalCode(r.getCenterCode());

                                Optional.ofNullable(r.getScheduledTime())
                                        .ifPresent(st -> orderCancelledCanonical.setConfirmedSchedule(DateUtils.getLocalDateTimeWithFormat(st)));

                                orderCancelledCanonical.setLocal(r.getCenterName());

                                orderCancelledCanonical.setServiceCode(r.getServiceTypeCode());
                                orderCancelledCanonical.setServiceName(r.getServiceTypeName());
                                orderCancelledCanonical.setServiceType(r.getServiceType());

                                orderCancelledCanonical.setStatusCode(s.getOrderStatus().getCode());
                                orderCancelledCanonical.setStatusName(s.getOrderStatus().getName());
                                orderCancelledCanonical.setStatusDetail(s.getOrderStatus().getDetail());

                                auditAdapter.updateExternalAudit(r.getSendNewFlow(), s, null).subscribe();                 

                                return orderCancelledCanonical;
                            }).defaultIfEmpty(
                                    new OrderCancelledCanonical(
                                            r.getEcommerceId(),
                                            Constant.OrderStatus.EMPTY_RESULT_CANCELLATION.getCode(),
                                            Constant.OrderStatus.EMPTY_RESULT_CANCELLATION.name())
                            );

                })
                .ordered((o1,o2) -> o2.getEcommerceId().intValue() - o1.getEcommerceId().intValue())
                .doOnComplete(() -> log.info("[END] cancelOrderProcess"));
    }

    public ResponseCanonical updateShoppingCartStatusAndNotes(ShoppingCartStatusCanonical shoppingCartStatusCanonical) {
        ResponseCanonical responseCanonical = new ResponseCanonical();
        try{
            RestTemplate restTemplate = new RestTemplate();
            String restoreStockUrl = externalServicesProperties.getUriApiRestoreStock();
            log.info("calling Insink service: {}", restoreStockUrl);
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("orderId", shoppingCartStatusCanonical.getId());
            restTemplate.exchange(restoreStockUrl, HttpMethod.PUT, HttpEntity.EMPTY, String.class, parameters);
            responseCanonical.setCode("200");
        }catch (Exception e) {
            e.getStackTrace();
            responseCanonical.setCode("500");
        }
        return responseCanonical;
    }

}
