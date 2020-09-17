package com.inretailpharma.digital.deliverymanager.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancelledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderResponseCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.dto.PartialOrderDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(value = "/fulfillment")
@Slf4j
public class DeliveryManagerRest {

    private DeliveryManagerFacade deliveryManagerFacade;

    public DeliveryManagerRest(DeliveryManagerFacade deliveryManagerFacade) {
        this.deliveryManagerFacade = deliveryManagerFacade;
    }

    @GetMapping(value = "/orders")
    public ResponseEntity<Flux<OrderCanonical>> listOrders(@RequestParam(name="status") String status) {
        log.info("[START] endpoint /orders - status:{}",status);

        return new ResponseEntity<>(
                deliveryManagerFacade.getOrdersByStatus(status)
                .subscribeOn(Schedulers.parallel()), HttpStatus.OK
                );
    }

    @PostMapping(value = "/order")
    public Mono<ResponseEntity<OrderCanonical>> createOrderReactive(@RequestBody OrderDto orderDto) {
        log.info("[START] endpoint /fulfillment/order - orderDto:{}",orderDto);
        return deliveryManagerFacade
                .createOrder(orderDto)
                .map(r -> ResponseEntity
                                .status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_STREAM_JSON)
                                .body(r)
                )
                .doOnSuccess(r -> log.info("[END] endpoint /fulfillment/order"))
                .subscribeOn(Schedulers.parallel());

    }

    @PatchMapping("/order/{ecommerceId}")
    public Mono<ResponseEntity<OrderCanonical>> updateStatusOrder(
            @PathVariable(value = "ecommerceId") String ecommerceId,
            @RequestBody ActionDto action) {

        log.info("[START] endpoint updateStatus /order/{ecommerceId} - ecommerceId {} - action {}"
                ,ecommerceId,action);

        return deliveryManagerFacade
                .getUpdateOrder(action, ecommerceId)
                .map(r -> ResponseEntity
                            .status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                            .body(r))
                .doOnSuccess(r -> log.info("[END] endpoint updateStatus /order/{ecommerceId}"))
                .subscribeOn(Schedulers.parallel());

    }

    @PostMapping("/order/partial/")
    public Mono<OrderCanonical> updatePartialOrder(@RequestBody OrderDto partialOrderDto) {
        log.info("[START] endpoint updatePartialOrder /order/partial/{} - partialOrderDto: {}",
                partialOrderDto);

        return deliveryManagerFacade.getUpdatePartialOrder(partialOrderDto);
    }

    @GetMapping(value = "/order/{orderNumber}",produces=MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<OrderResponseCanonical>> getOrderByOrderNumber(@PathVariable Long orderNumber) {
        log.info("[START] endpoint /fulfillment/order/{orderNumber}:{}",orderNumber);
        return deliveryManagerFacade.getOrderByOrderNumber(orderNumber)
        		.map(r -> ResponseEntity
                                .status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_STREAM_JSON)
                                .body(r)
                )
        		.doOnError(e -> log.info("error on response entity:"+e))
                .doOnSuccess(r -> log.info("[END] endpoint /fulfillment/order/{orderNumber}"))
                .subscribeOn(Schedulers.parallel());
    }
}
