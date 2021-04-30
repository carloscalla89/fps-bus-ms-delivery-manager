package com.inretailpharma.digital.deliverymanager.facade;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.HistorySynchronizedDto;
import com.inretailpharma.digital.deliverymanager.entity.CancellationCodeReason;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import reactor.core.publisher.Mono;

public interface OrderFacadeProxy {


    Mono<OrderCanonical> sendToUpdateOrder(IOrderFulfillment iOrderFulfillment, ActionDto actionDto,
                                           CancellationCodeReason codeReason);

    Mono<OrderCanonical> sendOnlyLastStatusOrderFromSync(IOrderFulfillment iOrdersFulfillment, ActionDto actionDto,
                                                         CancellationCodeReason codeReason);

    Mono<OrderCanonical> updateOrderStatusListAudit(IOrderFulfillment iOrdersFulfillment, OrderCanonical orderSend,
                                                    HistorySynchronizedDto historySynchronized, String origin);

    Mono<OrderCanonical> getfromOnlinePaymentExternalServices(Long orderId, Long ecommercePurchaseId, String source,
                                                              String serviceTypeShortCode, String companyCode,
                                                              ActionDto actionDto);

}
