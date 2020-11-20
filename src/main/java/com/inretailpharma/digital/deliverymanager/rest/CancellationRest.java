package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancelledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.facade.CancellationFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping(value = "/fulfillment/cancellation")
@Slf4j
public class CancellationRest {

    private CancellationFacade cancellationFacade;

    public CancellationRest(CancellationFacade cancellationFacade) {
        this.cancellationFacade = cancellationFacade;
    }

    @GetMapping("/reason")
    public ResponseEntity<Flux<CancellationCanonical>> getCancellationReasonsCode(
            @RequestParam(name="appType") String appType) {

        log.info("[START] endpoint getCancellationReasonsCode");

        return new ResponseEntity<>(
                cancellationFacade.getOrderCancellationList(appType)
                        .subscribeOn(Schedulers.parallel())
                        .doOnComplete(() -> log.info("[END] endpoint getCancellationReasonsCode"))
                , HttpStatus.OK
        );

    }

    @PutMapping("/orders")
    public Flux<OrderCancelledCanonical> cancelOrderProcess(
            @RequestBody CancellationDto cancellationDto) {
        log.info("[START] endpoint cancelOrderProcess /cancellation/orders - cancellationDto {}",cancellationDto);

        return cancellationFacade.cancelOrderProcess(cancellationDto)
                .doOnComplete(() -> log.info("[END] endpoint cancelOrderProcess /cancellation/orders"));
    }
}
