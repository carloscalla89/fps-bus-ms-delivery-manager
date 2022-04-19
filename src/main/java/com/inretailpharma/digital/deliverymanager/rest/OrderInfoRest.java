package com.inretailpharma.digital.deliverymanager.rest;


import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrderCanonicalResponse;
import com.inretailpharma.digital.deliverymanager.canonical.fulfillmentcenter.OrdersSelectedResponse;
import com.inretailpharma.digital.deliverymanager.dto.FilterOrderDTO;
import com.inretailpharma.digital.deliverymanager.dto.OrderInfoConsolidated;
import com.inretailpharma.digital.deliverymanager.dto.OrderStatusDto;
import com.inretailpharma.digital.deliverymanager.dto.RequestFilterDTO;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(value = "/order")
@Slf4j
@AllArgsConstructor
public class OrderInfoRest {

    private DeliveryManagerFacade deliveryManagerFacade;

    @PostMapping(value = "/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderCanonicalResponse getOrder(@RequestBody RequestFilterDTO filter) {
        log.info("[START] endpoint /fulfillment/order {}");
        return deliveryManagerFacade.getOrder(filter);
    }

    @PostMapping(value = "/selected", produces = MediaType.APPLICATION_JSON_VALUE)
    public OrdersSelectedResponse getListOrderById(@RequestBody FilterOrderDTO filter) {
        log.info("[START] endpoint /fulfillment/ [order selected] {}");

        return deliveryManagerFacade.getListOrderById(filter);
    }

    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<OrderStatusDto> getOrderStatus() {
        log.info("[START] endpoint /fulfillment/orderStatus {}");
        return deliveryManagerFacade.getAllOrderStatus().subscribeOn(Schedulers.parallel());

    }

    @GetMapping(value = "/detail/{ecommerceId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<OrderInfoConsolidated> getOrderInfoClient(@PathVariable(value = "ecommerceId") long ecommerceId) {
        log.info("[START] endpoint /order/detail/client");
        return deliveryManagerFacade.getOrderInfoDetail(ecommerceId);
    }
}
