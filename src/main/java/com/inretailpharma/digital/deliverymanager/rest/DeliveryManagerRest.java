package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.entity.projection.IOrderFulfillment;
import com.inretailpharma.digital.deliverymanager.errorhandling.ServerResponseError;
import com.inretailpharma.digital.deliverymanager.mangepartner.client.ManagePartnerClient;
import com.inretailpharma.digital.deliverymanager.util.Constant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/fulfillment")
@Slf4j
public class DeliveryManagerRest {

    private DeliveryManagerFacade deliveryManagerFacade;

    @Autowired
    private ManagePartnerClient managePartnerClient;

    public DeliveryManagerRest(DeliveryManagerFacade deliveryManagerFacade) {
        this.deliveryManagerFacade = deliveryManagerFacade;
    }

    @PatchMapping("/order/{ecommerceId}")
    public Mono<ResponseEntity<OrderCanonical>> updateStatusOrder(
            @PathVariable(value = "ecommerceId") String ecommerceId,
            @RequestBody ActionDto action) {


        log.info("[START] endpoint updateStatus /order/{} - ecommerceId - action {}"
                ,ecommerceId,action);

        // Notify status to Manage-partner component.
        Long ecommercePurchaseId = Long.parseLong(ecommerceId);
        IOrderFulfillment order = deliveryManagerFacade.getOrderByEcommerceID(ecommercePurchaseId);
        if (order != null && order.getSource().equalsIgnoreCase(Constant.SOURCE_RAPPI)) {
            List<String> orderStatuses = Arrays.asList(Constant.ORDER_STATUS_RAPPI);
            if (orderStatuses.contains(action.getAction())) {
                managePartnerClient.notifyEvent(ecommerceId, action);
            }
        }

        return deliveryManagerFacade
                .getUpdateOrder(action, ecommerceId)
                .map(r -> ResponseEntity
                            .status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
                            .body(r))
                .doOnSuccess(r -> log.info("[END] endpoint updateStatus /order/{}",ecommerceId))
                .subscribeOn(Schedulers.parallel());

    }

    @PostMapping("/order/partial/")
    public Mono<OrderCanonical> updatePartialOrder(@RequestBody OrderDto partialOrderDto) {
        log.info("[START] endpoint updatePartialOrder /order/partial/ - partialOrderDto: {}",
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
