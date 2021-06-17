package com.inretailpharma.digital.deliverymanager.adapter;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.transactions.OrderTransaction;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.inretailpharma.digital.deliverymanager.util.Constant.OrderStatus.SUCCESS_RESULT_ONLINE_PAYMENT;

@Slf4j
@Component
public class PaymentAdapter implements IPaymentAdapter{

    private OrderExternalService externalOnlinePaymentService;
    private OrderTransaction orderTransaction;

    @Autowired
    public PaymentAdapter(@Qualifier("onlinePayment") OrderExternalService externalOnlinePaymentService,
                          OrderTransaction orderTransaction) {
        this.externalOnlinePaymentService = externalOnlinePaymentService;
        this.orderTransaction = orderTransaction;

    }

    @Override
    public Mono<OrderCanonical> getfromOnlinePayment(IOrderFulfillment iOrderFulfillment, ActionDto actionDto) {
        return externalOnlinePaymentService
                .getResultfromOnlinePaymentExternalServices(iOrderFulfillment.getEcommerceId(), iOrderFulfillment.getSource(),
                        iOrderFulfillment.getServiceTypeShortCode(), iOrderFulfillment.getCompanyCode(), actionDto)
                .map(r -> {
                    log.info("[START] to update online payment order = {}", r);
                    String onlinePaymentStatus = "";
                    if(SUCCESS_RESULT_ONLINE_PAYMENT.getCode().equals(r.getOrderStatus().getCode())) {
                        Constant.OrderStatus status = Constant.OrderStatus.getByName(actionDto.getAction());
                        OrderStatusCanonical paymentRsp = new OrderStatusCanonical();
                        paymentRsp.setCode(status.getCode());
                        paymentRsp.setName(status.name());
                        r.setOrderStatus(paymentRsp);
                        onlinePaymentStatus = actionDto.getAction().equalsIgnoreCase(Constant.CANCEL_ORDER) ?  Constant.OnlinePayment.CANCELLED:Constant.OnlinePayment.LIQUIDETED;
                        log.info("[PROCESS] to update online payment order::{}, status::{}", iOrderFulfillment.getOrderId(), onlinePaymentStatus);
                        orderTransaction.updateOrderOnlinePaymentStatusByExternalId(iOrderFulfillment.getOrderId(),onlinePaymentStatus);
                    }
                    log.info("[END] to update order");
                    return r;
                });
    }
}
