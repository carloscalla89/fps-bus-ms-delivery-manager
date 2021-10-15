package com.inretailpharma.digital.deliverymanager.adapter;


import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalSellerCenterProperties;
import com.inretailpharma.digital.deliverymanager.proxy.OrderExternalService;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SellerCenterAdapter extends AdapterAbstractUtil implements ISellerCenterAdapter {


    private OrderExternalService sellerCenterExternalService;

    @Autowired
    public SellerCenterAdapter(@Qualifier("sellerCenterService") OrderExternalService sellerCenterExternalService) {
        this.sellerCenterExternalService = sellerCenterExternalService;
    }

    @Override
    public Mono<OrderCanonical> updateStatusOrderSeller(Long ecommerceId, String actionName) {
        log.info("[START] updateStatusOrderSeller");
        Constant.OrderStatusTracker orderStatusTracker = Constant.OrderStatusTracker.getByActionName(actionName);

        String statusToSend = orderStatusTracker.getTrackerLiteStatus();

        sellerCenterExternalService.updateStatusOrderSeller(ecommerceId, statusToSend).subscribe();

        OrderCanonical orderCanonical = new OrderCanonical();
        orderCanonical.setEcommerceId(ecommerceId);
        orderCanonical.setTarget(Constant.TARGET_SELLER);

        OrderStatusCanonical orderStatusCanonical = new OrderStatusCanonical();
        orderStatusCanonical.setCode(orderStatusTracker.getOrderStatus().getCode());
        orderStatusCanonical.setName(orderStatusTracker.getOrderStatus().name());

        orderCanonical.setOrderStatus(orderStatusCanonical);
        log.info("[END] updateStatusOrderSeller");
        return Mono.just(orderCanonical);

    }
}
