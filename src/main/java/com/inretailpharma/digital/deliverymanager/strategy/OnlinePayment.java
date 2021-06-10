package com.inretailpharma.digital.deliverymanager.strategy;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.facade.FacadeAbstractUtil;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.inretailpharma.digital.deliverymanager.util.Constant.OrderStatus.SUCCESS_RESULT_ONLINE_PAYMENT;

@Slf4j
@Component("payment")
public class OnlinePayment extends FacadeAbstractUtil implements IActionStrategy {

    private OrderExternalService externalOnlinePaymentService;

    public OnlinePayment() {

    }

    @Autowired
    public OnlinePayment(@Qualifier("onlinePayment") OrderExternalService externalOnlinePaymentService) {

        this.externalOnlinePaymentService = externalOnlinePaymentService;
    }

    @Override
    public boolean validationIfExistOrder(Long ecommerceId, ActionDto actionDto) {
        return getOnlyOrderStatusFinalByecommerceId(ecommerceId);
    }

    @Override
    public Mono<OrderCanonical> process(ActionDto actionDto, Long ecommerceId) {

        IOrderFulfillment iOrderFulfillmentLight = getOrderLightByecommerceId(ecommerceId);

        return externalOnlinePaymentService
                .getResultfromOnlinePaymentExternalServices(
                        iOrderFulfillmentLight.getEcommerceId(), iOrderFulfillmentLight.getSource(),
                        iOrderFulfillmentLight.getServiceTypeShortCode(), iOrderFulfillmentLight.getCompanyCode(), actionDto)
                .map(r -> {
                    log.info("[START] to update online payment order = {}", r);

                    if(SUCCESS_RESULT_ONLINE_PAYMENT.getCode().equals(r.getOrderStatus().getCode())) {
                        Constant.OrderStatus status = Constant.OrderStatus.getByName(actionDto.getAction());
                        OrderStatusCanonical paymentRsp = new OrderStatusCanonical();
                        paymentRsp.setCode(status.getCode());
                        paymentRsp.setName(status.name());
                        r.setOrderStatus(paymentRsp);
                        String onlinePaymentStatus = Constant.OnlinePayment.LIQUIDETED;
                        log.info("[PROCESS] to update online payment order::{}, status::{}",
                                iOrderFulfillmentLight.getOrderId(), onlinePaymentStatus);
                        updateOrderOnlinePaymentStatusByExternalId(iOrderFulfillmentLight.getOrderId(),onlinePaymentStatus);
                    }
                    log.info("[END] to update order");
                    return r;
                });

    }
}
