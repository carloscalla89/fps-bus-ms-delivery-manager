package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.manager.*;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.facade.CancellationFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

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
            @RequestParam(name="appType") String appType
            ,@RequestParam(name="type", required = false) String type) {

        log.info("[START] endpoint getCancellationReasonsCode");

        return new ResponseEntity<>(
                cancellationFacade.getOrderCancellationList(appType, type)
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

    @ResponseBody
    @PostMapping("/restore/stock")
    public ResponseCanonical addCartInformation(@RequestBody ShoppingCartStatusCanonical shoppingCartStatusCanonical) {
        try {
            log.info("************** START [ /shoppingcart/setstatus ] **************");
            log.info("#cancel- proccess");
            log.info("ShoppingCartStatusCanonical: {}", shoppingCartStatusCanonical);
            ResponseCanonical responseCanonical = cancellationFacade.updateShoppingCartStatusAndNotes(shoppingCartStatusCanonical);
            log.info("ShoppingCartStatusCanonical: {}", responseCanonical);
            return responseCanonical;
        } finally {
            log.info("************** END [ /shoppingcart/setstatus ] **************");
        }
    }
}
