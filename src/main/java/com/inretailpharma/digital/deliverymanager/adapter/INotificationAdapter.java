package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface INotificationAdapter {

    Mono<Boolean> sendNotification(ActionDto actionDto, IOrderFulfillment iOrderFulfillment);
}
