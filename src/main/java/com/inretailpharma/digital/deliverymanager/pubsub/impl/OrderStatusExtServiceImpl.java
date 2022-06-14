package com.inretailpharma.digital.deliverymanager.pubsub.impl;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.AuditHistoryDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.pubsub.OrderStatusService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gcp.pubsub.support.GcpPubSubHeaders;
import org.springframework.cloud.gcp.pubsub.support.converter.ConvertedBasicAcknowledgeablePubsubMessage;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;

import reactor.core.publisher.Mono;

@Slf4j
@EnableBinding(OrderStatusService.class)
public class OrderStatusExtServiceImpl {

    private DeliveryManagerFacade deliveryManagerFacade;
    private OrderExternalService orderExternalServiceAudit;

    public OrderStatusExtServiceImpl(DeliveryManagerFacade deliveryManagerFacade,
    		@Qualifier("audit") OrderExternalService orderExternalServiceAudit) {
        this.deliveryManagerFacade = deliveryManagerFacade;
        this.orderExternalServiceAudit = orderExternalServiceAudit;
    }

    @StreamListener(OrderStatusService.CHANNEL)
    public void handleMessage(ActionDto payload, @Headers MessageHeaders headers) {

        log.info("[START] OrderStatusServiceImpl - payload {}", payload);
        
        var message = headers.get(GcpPubSubHeaders.ORIGINAL_MESSAGE, ConvertedBasicAcknowledgeablePubsubMessage.class);
        
        if (payload != null) {


	        var result = deliveryManagerFacade
	                .getUpdateOrder(payload, String.valueOf(payload.getEcommerceId()), false)
	                .onErrorResume(ex -> {
	                	
	                	message.nack();	                	
	                	
	                	var auditHistoryDto = new AuditHistoryDto();
	                	auditHistoryDto.setEcommerceId(payload.getEcommerceId());
                        auditHistoryDto.setStatusCode(Constant.ERROR_UPDATE_CODE);
                        auditHistoryDto.setSource(Constant.ORIGIN_UNIFIED_POS);
                        auditHistoryDto.setStatusDetail(ex.getMessage());
                        auditHistoryDto.setTimeFromUi(DateUtils.getLocalDateTimeNow());
                        auditHistoryDto.setUpdatedBy(payload.getUpdatedBy());
                        orderExternalServiceAudit.updateOrderNewAudit(auditHistoryDto).subscribe();
	
	                    OrderStatusCanonical os = new OrderStatusCanonical();
	                    os.setName("ERROR");
	                    os.setDetail(ex.getMessage());
	                    OrderCanonical order = new OrderCanonical();
	                    order.setOrderStatus(os);
	                    order.setEcommerceId(payload.getEcommerceId());
	                    return Mono.just(order);
	                })
	                .block();        
    

        	log.info("[INFO] OrderStatusServiceImpl - result {}", result);
    	}
        
        log.info("[END] OrderStatusServiceImpl - payload {}", payload);

    }

}
