package com.inretailpharma.digital.deliverymanager.proxy;

import com.inretailpharma.digital.deliverymanager.canonical.OrderFulfillmentCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.OrderStatusCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.inkatrackerlite.OrderInfoCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.config.parameters.ExternalServicesProperties;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service("inkatrackerlite")
public class InkatrackerLiteServiceImpl implements OrderExternalService {

    private OrderExternalService orderExternalService;
    private ExternalServicesProperties externalServicesProperties;

    public InkatrackerLiteServiceImpl(ExternalServicesProperties externalServicesProperties,
                                      @Qualifier("audit") OrderExternalService orderExternalService) {
        this.externalServicesProperties = externalServicesProperties;
        this.orderExternalService = orderExternalService;
    }

    @Override
    public void sendOrder(OrderCanonical orderAuditCanonical) {

    }

    @Override
    public void updateOrder(OrderCanonical orderCanonical) {

    }

    @Override
    public OrderCanonical getResultfromExternalServices(Long ecommerceId, Constant.ActionOrder actionOrder) {
        log.info("[START] connect inkatracker-lite   - ecommerceId:{} - actionOrder:{}",
                ecommerceId, actionOrder);

        String actionInkatrackerLite;
        Constant.OrderStatus pending;
        Constant.OrderStatus successResponse;

        switch (actionOrder.name()) {

            case Constant.ActionName.RELEASE_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_BILLING;
                pending = Constant.OrderStatus.PENDING_RELEASE_ORDER;
                successResponse = Constant.OrderStatus.RELEASED_ORDER;
                break;
            case Constant.ActionName.CANCEL_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.CANCELLED;
                pending = Constant.OrderStatus.PENDING_CANCEL_ORDER;
                successResponse = Constant.OrderStatus.CANCELLED_ORDER;
                break;
            case Constant.ActionName.DELIVER_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.DELIVERED;
                pending = Constant.OrderStatus.PENDING_DELIVERY_ORDER;
                successResponse = Constant.OrderStatus.DELIVERED_ORDER;
                break;
            case Constant.ActionName.READY_PICKUP_ORDER:
                actionInkatrackerLite = Constant.ActionNameInkatrackerlite.READY_FOR_PICKUP;
                pending = Constant.OrderStatus.PENDING_READY_PICKUP_ORDER;
                successResponse = Constant.OrderStatus.READY_PICKUP_ORDER;
                break;
            default:
                actionInkatrackerLite = Constant.OrderStatus.NOT_FOUND_ACTION.name();
                pending = Constant.OrderStatus.NOT_DEFINED_STATUS;
                successResponse = Constant.OrderStatus.NOT_DEFINED_STATUS;
        }


        Mono<OrderCanonical> webClient =        WebClient
                        .create(externalServicesProperties.getInkatrackerLiteUpdateOrderUri())
                        .patch()
                        //.uri("/{externalId}",ecommerceId.toString())

                        .uri(builder ->
                                builder
                                        .path("/{orderExternalId}").queryParam("action",actionInkatrackerLite)
                                        .build(ecommerceId))
                        .retrieve()
                        .bodyToMono(OrderInfoCanonical.class)
                        .map(r -> {
                            OrderCanonical orderCanonical = new OrderCanonical();

                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                            orderStatus.setCode(successResponse.getCode());
                            orderStatus.setName(successResponse.name());
                            orderCanonical.setOrderStatus(orderStatus);

                            return orderCanonical;
                        })
                        .onErrorResume(e -> {
                            e.printStackTrace();
                            log.error("Error in audit call {} ",e.getMessage());
                            OrderCanonical orderCanonical = new OrderCanonical();

                            OrderStatusCanonical orderStatus = new OrderStatusCanonical();
                            orderStatus.setCode(Constant.OrderStatus.ERROR_UPDATE_ORDER.getCode());
                            orderStatus.setName(Constant.OrderStatus.ERROR_UPDATE_ORDER.name());
                            orderStatus.setDetail(e.getMessage());

                            orderCanonical.setOrderStatus(orderStatus);

                            return Mono.just(orderCanonical);
                        });


        webClient.subscribe(r -> {
            log.info("Response inkatracker lite {} ",r);
            r.setEcommerceId(ecommerceId);
            orderExternalService.updateOrder(r);
        });

        OrderCanonical orderCanonical = new OrderCanonical();
        OrderStatusCanonical orderStatus = new OrderStatusCanonical();
        orderStatus.setCode(pending.getCode());
        orderStatus.setName(pending.name());

        orderCanonical.setOrderStatus(orderStatus);

        return orderCanonical;
    }
}
