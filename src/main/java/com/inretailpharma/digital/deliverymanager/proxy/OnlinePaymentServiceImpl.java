package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.OrderInkatrackerCanonical;
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
        
        String action = actionDto.getAction();

        String onlinePaymentUri = null;
        Integer connetTimeout;
        Long readTimeout;
        
        switch (action) {
        	case Constant.CANCEL_ORDER:   
        		
        	     onlinePaymentUri = externalServicesProperties.getOnlinePaymentRejectedUri();        	       
        		 connetTimeout = Integer.parseInt(externalServicesProperties.getOnlinePaymentRejectedConnectTimeOut());
        		 readTimeout = Long.parseLong(externalServicesProperties.getOnlinePaymentRejectedReadTimeOut());
        		 break;
        	default:
        		
    	      onlinePaymentUri = externalServicesProperties.getOnlinePaymentLiquidatedUri();    	        
    		  connetTimeout = Integer.parseInt(externalServicesProperties.getOnlinePaymentLiquidatedConnectTimeOut());
    		  readTimeout = Long.parseLong(externalServicesProperties.getOnlinePaymentLiquidatedReadTimeOut());
        		
        }        
       

        log.info("ecommercePurchaseId::{}, url:{}", ecommercePurchaseId, onlinePaymentUri);

        return WebClient
                .builder()
                .clientConnector(
                        generateClientConnector(
                        		connetTimeout,
                        		readTimeout
                        )
                )
                .baseUrl(onlinePaymentUri)
                .build()
                .post()
                .body(Mono.just(onlinePaymentOrder), OnlinePaymentOrder.class)
                .retrieve()
                .bodyToMono(OrderCanonical.class)
                .doOnSuccess(r -> log.info("[SUCCESS] call to online Payment - response"))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    log.info("[ERROR] call to online Payment - response {}", e.getMessage());
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
