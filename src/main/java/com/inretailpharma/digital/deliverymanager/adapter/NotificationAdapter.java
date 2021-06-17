package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import com.inretailpharma.digital.deliverymanager.util.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import reactor.core.publisher.Mono;

@Slf4j
public class NotificationAdapter extends AdapterAbstractUtil implements INotificationAdapter {

    private OrderExternalService notificationExternalService;

    @Autowired
    public NotificationAdapter(@Qualifier("notification") OrderExternalService notificationExternalService) {
        this.notificationExternalService = notificationExternalService;
    }

    @Override
    public Mono<Boolean> sendNotification(ActionDto actionDto, IOrderFulfillment iOrderFulfillment) {

        log.info("sendNotification:{}",iOrderFulfillment.getSendNotificationByChannel());

        if (iOrderFulfillment.getSendNotificationByChannel()) {

            String statusToSend = Constant
                    .OrderStatusTracker
                    .getByActionNameAndServiceTypeCoce(actionDto.getAction(), iOrderFulfillment.getClassImplement());

            String localType = Constant.TrackerImplementation.getClassImplement(iOrderFulfillment.getClassImplement()).getLocalType();

            String expiredDate = null;

            if (Constant.PICKUP.equalsIgnoreCase(iOrderFulfillment.getServiceType())
                    && Constant.ActionOrder.READY_PICKUP_ORDER.name().equalsIgnoreCase(actionDto.getAction())) {

                int daysToExpiredRet = Integer
                        .parseInt(getValueOfParameter(Constant.ApplicationsParameters.DAYS_PICKUP_MAX_RET));

                expiredDate = DateUtils.getLocalDateWithFormat(iOrderFulfillment.getScheduledTime().plusDays(daysToExpiredRet));

                log.info("Get expired date:{}",expiredDate);
            }

            notificationExternalService
                    .sendNotification(getDtoToNotification(iOrderFulfillment, statusToSend, expiredDate, localType))
                    .subscribe();

        }

        return Mono.just(true);
    }
}
