package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.dto.notification.MessageDto;
import com.inretailpharma.digital.deliverymanager.dto.notification.PayloadDto;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service("notificationadapter")
public class NotificationAdapterImpl extends AdapterAbstract implements AdapterInterface {

    private OrderExternalService notificationExternalService;

    @Autowired
    public NotificationAdapterImpl(@Qualifier("notification") OrderExternalService notificationExternalService) {
        this.notificationExternalService = notificationExternalService;
    }

    @Override
    public Mono<Void> sendNotification(String channel, String serviceTypeCode, String orderStatus, Long ecommerceId,
                                       String brand, String localCode, String localTypeCode, String phoneNumber,
                                       String clientName, String expiredDate, String confirmedDate, String address) {

        MessageDto messageDto = new MessageDto();
        messageDto.setOrderId(ecommerceId.toString());
        messageDto.setBrand(brand);
        messageDto.setChannel(channel);
        messageDto.setDeliveryTypeCode(serviceTypeCode);
        messageDto.setLocalTypeCode(localTypeCode);
        messageDto.setOrderStatus(orderStatus);
        messageDto.setPhoneNumber(phoneNumber);

        PayloadDto payloadDto = new PayloadDto();
        payloadDto.setClientName(clientName);
        payloadDto.setAddress(address);

        messageDto.setPayload(payloadDto);

        notificationExternalService
                .sendNotification(messageDto)
                .subscribe(response -> log.info("Response notification service:{} with request:{}",response,messageDto));

        return Mono.when(Mono.just(Constant.SUCCESS));
    }



}
