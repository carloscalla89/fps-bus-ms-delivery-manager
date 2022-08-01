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
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Component
public class CancellationFacade extends FacadeAbstractUtil{

    private ExternalServicesProperties externalServicesProperties;
    private DeliveryManagerFacade deliveryManagerFacade;
    private OrderExternalService stockService;

    @Autowired
    public CancellationFacade(ExternalServicesProperties externalServicesProperties,
                              DeliveryManagerFacade deliveryManagerFacade,
                              @Qualifier("stock")OrderExternalService stockService) {

        this.externalServicesProperties = externalServicesProperties;
        this.deliveryManagerFacade = deliveryManagerFacade;
        this.stockService = stockService;
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

                    return deliveryManagerFacade
                            .getUpdateOrder(actionDto, r.getEcommerceId().toString(), true)
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

    public Mono<ResponseCanonical> updateShoppingCartStatusAndNotes(ShoppingCartStatusCanonical shoppingCartStatusCanonical) {

    	stockService.releaseStock(shoppingCartStatusCanonical.getId()).subscribe();
    	ResponseCanonical responseCanonical = new ResponseCanonical();
    	responseCanonical.setCode("200");
    	return Mono.just(responseCanonical);

    }

}
