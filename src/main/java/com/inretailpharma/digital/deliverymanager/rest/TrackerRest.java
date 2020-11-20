package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderAssignResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderToAssignCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.inretailpharma.digital.deliverymanager.facade.TrackerFacade;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(value = "/fulfillment/tracker")
@Slf4j
public class TrackerRest {

    private TrackerFacade trackerFacade;

    public TrackerRest(TrackerFacade trackerFacade) {
        this.trackerFacade = trackerFacade;
    }

    @PostMapping(value = "/orders/status/assigned")
    public ResponseEntity<Mono<OrderAssignResponseCanonical>> assignOrders(
            @RequestBody ProjectedGroupCanonical projectedGroupCanonical) {

        log.info("[START] endpoint /fulfillment/tracker/orders/status/assigned " +
                "- projectedGroupCanonical:{}",projectedGroupCanonical);

        return new ResponseEntity<>(
                trackerFacade.assignOrders(projectedGroupCanonical)
                        .subscribeOn(Schedulers.parallel()),
                HttpStatus.OK
        );

    }

    @PatchMapping(value = "/orders/status/unassigned")
    public ResponseEntity<Mono<OrderTrackerResponseCanonical>> unassignOrders(
            @RequestBody UnassignedCanonical unassignedCanonical) {

        log.info("[START] endpoint /fulfillment/tracker/orders/status/unassigned " +
                "- unassignedCanonical:{}", unassignedCanonical);

        return new ResponseEntity<>(
                trackerFacade.unassignOrders(unassignedCanonical)
                        .subscribeOn(Schedulers.parallel()),
                HttpStatus.OK
        );

    }

    @PatchMapping(value = "/order/{ecommerceId}/status/{status}")
    public ResponseEntity<Mono<OrderTrackerResponseCanonical>> updateOrderStatus(
            @PathVariable(name = "ecommerceId") Long ecommerceId,
            @PathVariable(name = "status") String status) {

        log.info("[START] endpoint /order/{ecommerceId}/status/{status} " +
                "- ecommerceId {} - status:{}", ecommerceId, status);

        return new ResponseEntity<>(
                trackerFacade.updateOrderStatus(ecommerceId, status)
                        .subscribeOn(Schedulers.parallel()),
                HttpStatus.OK
        );

    }

    @PostMapping(value = "/order")
    public ResponseEntity<Mono<OrderTrackerResponseCanonical>> sendOrder(
            @RequestBody OrderToAssignCanonical orderToAssignCanonical) {

        log.info("[START] endpoint /fulfillment/tracker/order " +
                "- orderToAssignCanonical:{}", orderToAssignCanonical);

        return new ResponseEntity<>(
                trackerFacade.sendOrder(orderToAssignCanonical)
                        .subscribeOn(Schedulers.parallel()),
                HttpStatus.OK
        );

    }

}
