package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.inkatracker.ProjectedGroupCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.ordertracker.OrderTrackerResponseCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.facade.TrackerFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
@RequestMapping(value = "/fulfillment/tracker")
@Api(value = "TrackerRest", produces = "application/json")
@Slf4j
public class TrackerRest {

    private TrackerFacade trackerFacade;

    public TrackerRest(TrackerFacade trackerFacade) {
        this.trackerFacade = trackerFacade;
    }

    @ApiOperation(value = "Asignar órdenes a motorizados")
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Asignar órdenes a motorizados", response = OrderDto.class),
            @ApiResponse(code = 500, message = "No creado") })
    @PostMapping(value = "/shipper/orders")
    public ResponseEntity<Mono<OrderTrackerResponseCanonical>> listOrders(
            @RequestBody ProjectedGroupCanonical projectedGroupCanonical) {

        log.info("[START] endpoint /fulfillment/tracker/shipper/orders " +
                 "- projectedGroupCanonical:{}",projectedGroupCanonical);

        return new ResponseEntity<>(
                trackerFacade.assignShipper(projectedGroupCanonical)
                             .subscribeOn(Schedulers.parallel()),
                HttpStatus.OK
        );

    }
}
