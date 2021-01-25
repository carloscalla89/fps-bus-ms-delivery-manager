package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.onlinepayment.OnlinePaymentOrder;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
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
    public Mono<OrderCanonical> getResultfromOnlinePaymentExternalServices(Long ecommercePurchaseId, String source,
                                                                           String serviceTypeShortCode, String companyCode,
                                                                           ActionDto actionDto) {

        OnlinePaymentOrder onlinePaymentOrder = new OnlinePaymentOrder();
        onlinePaymentOrder.setEcommerceExternalId(String.valueOf(ecommercePurchaseId));
        onlinePaymentOrder.setSource(source);
        onlinePaymentOrder.setServiceTypeShortCode(serviceTypeShortCode);

        String onlinePaymentUri = null;
        switch (companyCode) {
            case Constant.Constans.COMPANY_CODE_IFK:
                onlinePaymentUri = externalServicesProperties.getOnlinePaymentLiquidatedUri();
            case Constant.Constans.COMPANY_CODE_MF:
                onlinePaymentUri = externalServicesProperties.getOnlinePaymentLiquidatedUriMifa();
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
