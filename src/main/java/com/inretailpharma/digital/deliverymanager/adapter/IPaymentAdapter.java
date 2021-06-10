package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import reactor.core.publisher.Mono;

public interface IPaymentAdapter {

    Mono<OrderCanonical> getfromOnlinePayment(IOrderFulfillment iOrderFulfillment, ActionDto actionDto);
}
