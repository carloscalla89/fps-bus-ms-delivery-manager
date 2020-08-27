package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.manager.CancellationCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancelledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.facade.CancellationFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(value = "CancellationRest", produces = "application/json")
@Slf4j
public class CancellationRest {

    private CancellationFacade cancellationFacade;

    public CancellationRest(CancellationFacade cancellationFacade) {
        this.cancellationFacade = cancellationFacade;
    }

    @ApiOperation(value = "Obtener los códigos y descripción de cancelación de una orden",
            tags = { "Controlador DeliveryManager" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Lista obtenida correctamente", response = List.class),
            @ApiResponse(code = 500, message = "No creado") })
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

    @ApiOperation(value = "cancelar órdenes que han excedido los días permitidos para entregar o recoger")
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Órdenes canceladas correctamente", response = OrderCancelledCanonical.class),
            @ApiResponse(code = 500, message = "No creado") })
    @PutMapping("/orders")
    public Flux<OrderCancelledCanonical> cancelOrderProcess(
            @RequestBody CancellationDto cancellationDto) {
        log.info("[START] endpoint cancelOrderProcess /cancellation/orders - cancellationDto {}",cancellationDto);

        return cancellationFacade.cancelOrderProcess(cancellationDto)
                .doOnComplete(() -> log.info("[END] endpoint cancelOrderProcess /cancellation/orders"));
    }
}
