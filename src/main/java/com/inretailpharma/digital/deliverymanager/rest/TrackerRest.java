package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderAssignResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderToAssignCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.UnassignedCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderSynchronizeDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.inretailpharma.digital.deliverymanager.facade.TrackerFacade;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

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

    @PatchMapping("/orders/synchronize")
    public Flux<OrderTrackerResponseCanonical> synchronizeOrderStatus(@RequestBody List<OrderSynchronizeDto> orders) {
        log.info("[START] endpoint synchronizeOrderStatus /orders/synchronize {}",orders);

        return trackerFacade
                .synchronizeOrderStatus(orders)
                .doOnComplete(() -> log.info("[END] endpoint synchronizeOrderStatus /orders/synchronize"));

    }

}
