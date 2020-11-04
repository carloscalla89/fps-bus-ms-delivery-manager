package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.onlinepayment.OnlinePaymentOrder;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service("onlinePayment")
public class OnlinePaymentServiceImpl extends AbstractOrderService implements OrderExternalService {

    private ExternalServicesProperties externalServicesProperties;

    public OnlinePaymentServiceImpl(ExternalServicesProperties externalServicesProperties) {
        this.externalServicesProperties = externalServicesProperties;
    }

    @Override
    public Mono<OrderCanonical> getResultfromOnlinePaymentExternalServices(Long ecommercePurchaseId, ActionDto actionDto) {

        OnlinePaymentOrder onlinePaymentOrder = new OnlinePaymentOrder();
        onlinePaymentOrder.setEcommerceExternalId(String.valueOf(ecommercePurchaseId));

        String onlinePaymentUri = StringUtils.EMPTY;
        switch (actionDto.getAction()) {
            case Constant.ActionName.LIQUIDATED_ONLINE_PAYMENT:
                onlinePaymentUri = externalServicesProperties.getOnlinePaymentLiquidatedUri();
                break;
            case Constant.ActionName.REJECTED_ONLINE_PAYMENT:
                onlinePaymentUri = externalServicesProperties.getOnlinePaymentLiquidatedUri();
                break;
            default:
        }

        log.info("ecommercePurchaseId::{}, url:{}", ecommercePurchaseId, onlinePaymentUri);

        return WebClient
                .create(onlinePaymentUri)
                .post()
                .bodyValue(onlinePaymentOrder)
                .retrieve()
                .bodyToMono(OrderCanonical.class)
                .doOnSuccess(r -> log.info("[SUCCESS] call to online Payment - response"))
                .onErrorResume(e -> {
                    log.info("[ERROR] call to online Payment - response {}", e);
                    return Mono.just(new OrderCanonical(
                            ecommercePurchaseId,
                            Constant.OrderStatus.ERROR_RESULT_ONLINE_PAYMENT.getCode(),
                            Constant.OrderStatus.ERROR_RESULT_ONLINE_PAYMENT.name()));
                }).defaultIfEmpty(
                        new OrderCanonical(
                                ecommercePurchaseId,
                                Constant.OrderStatus.SUCCESS_RESULT_ONLINE_PAYMENT.getCode(),
                                Constant.OrderStatus.SUCCESS_RESULT_ONLINE_PAYMENT.name())
                );
    }
}
