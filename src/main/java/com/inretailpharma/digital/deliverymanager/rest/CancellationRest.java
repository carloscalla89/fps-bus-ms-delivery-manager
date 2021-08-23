package com.inretailpharma.digital.deliverymanager.rest;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancelledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ResponseCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.ShoppingCartStatusCanonical;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.facade.CancellationFacade;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
            @RequestParam(name="appType") List<String> appType
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
    public ResponseEntity<Mono<ResponseCanonical>> addCartInformation(@RequestBody ShoppingCartStatusCanonical shoppingCartStatusCanonical) {
    	
    	log.info("************** START [ /shoppingcart/setstatus ] **************");
        log.info("#cancel- proccess");
        log.info("ShoppingCartStatusCanonical: {}", shoppingCartStatusCanonical);
        
        return new ResponseEntity<>(
        		cancellationFacade.updateShoppingCartStatusAndNotes(shoppingCartStatusCanonical)
                        .subscribeOn(Schedulers.parallel()),
                HttpStatus.OK
        );
    }
}
