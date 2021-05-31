package com.inretailpharma.digital.deliverymanager.facade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancelledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ShoppingCartStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.strategy.UpdateTracker;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class CancellationFacade extends FacadeAbstractUtil{

    private ExternalServicesProperties externalServicesProperties;
    private UpdateTracker updateTracker;

    @Autowired
    public CancellationFacade(ExternalServicesProperties externalServicesProperties,
                              @Qualifier("updateTracker") UpdateTracker updateTracker) {

        this.externalServicesProperties = externalServicesProperties;
        this.updateTracker = updateTracker;
    }

    public Flux<CancellationCanonical> getOrderCancellationList(List<String> appType, String type) {
        return Flux.fromIterable(getCancellationsCodeByAppTypeList(appType, type));
    }

    public Flux<OrderCancelledCanonical> cancelOrderProcess(CancellationDto cancellationDto) {
        log.info("[START] cancelOrderProcess");

        return Flux
                .fromIterable(
                        getListOrdersToCancel(
                                cancellationDto.getServiceType(), cancellationDto.getCompanyCode(),
                                Integer.parseInt(getValueOfParameter(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET)),
                                cancellationDto.getStatusType()
                        )
                )
                .publishOn(Schedulers.boundedElastic())
                .flatMap(r -> {

                    log.info("order info- companyCode:{}, centerCode:{}, ecommerceId:{}, serviceTypeCode:{}, getSendNewFlow:{} ",
                            r.getCompanyCode(), r.getCenterCode(), r.getEcommerceId(), r.getServiceTypeCode(), r.getSendNewFlow());

                    ActionDto actionDto = ActionDto
                                                .builder()
                                                .action(Constant.ActionOrder.CANCEL_ORDER.name())
                                                .orderCancelCode(cancellationDto.getCancellationCode())
                                                .orderCancelObservation(cancellationDto.getObservation())
                                                .origin(Constant.ORIGIN_TASK_EXPIRATION)
                                                .updatedBy(Constant.TASK_LAMBDA_UPDATED_BY)
                                                .build();

                    return updateTracker
                            .evaluate(actionDto,r.getEcommerceId().toString())
                            .defaultIfEmpty(
                                    new OrderCanonical(
                                            r.getEcommerceId(),
                                            Constant.OrderStatus.ERROR_CANCELLED.getCode(),
                                            Constant.OrderStatus.ERROR_CANCELLED.name()
                                    )
                            )
                            .filter(s -> s.getOrderStatus() != null)
                            .flux()
                            .flatMap(s -> {
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
                                
                                return Flux.just(orderCancelledCanonical);
                            }).defaultIfEmpty(
                                    new OrderCancelledCanonical(
                                            r.getEcommerceId(),
                                            Constant.OrderStatus.EMPTY_RESULT_CANCELLATION.getCode(),
                                            Constant.OrderStatus.EMPTY_RESULT_CANCELLATION.name())
                            );

                })
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
