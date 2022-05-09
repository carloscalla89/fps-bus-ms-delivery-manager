package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/fulfillment")
@Slf4j
public class StorePickupRest {

    @Autowired
    private DeliveryManagerFacade deliveryManagerFacade;

    @PatchMapping("/order/pickup")
    public Mono<ResponseEntity<OrderCanonical>> updateDataStorePickupOrder(
            @RequestBody OrderDto orderDto
    ) {
        log.info("[START] endpoint updateStorePickup /order/pickup: {}", orderDto);
        return deliveryManagerFacade.getUpdateOrderStorePickup(orderDto)
                .map(r -> ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(r))
                .doOnSuccess(r -> log.info("[END] endpoint /order/pickup "));
    }

}
