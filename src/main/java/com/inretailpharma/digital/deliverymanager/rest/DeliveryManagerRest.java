package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCancelledCanonical;
import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.CancellationDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import io.swagger.annotations.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@RestController
@RequestMapping(value = "/fulfillment")
@Api(value = "DeliveryManagerRest", produces = "application/json")
@Slf4j
public class DeliveryManagerRest {

    private DeliveryManagerFacade deliveryManagerFacade;

    public DeliveryManagerRest(DeliveryManagerFacade deliveryManagerFacade) {
        this.deliveryManagerFacade = deliveryManagerFacade;
    }

    @ApiOperation(value = "Lista de órdenes")
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Orden creado", response = OrderDto.class),
            @ApiResponse(code = 500, message = "No creado") })
    @GetMapping(value = "/orders")
    public ResponseEntity<Flux<OrderCanonical>> listOrders(@RequestParam(name="status") String status) {
        log.info("[START] endpoint /orders - status:{}",status);

        return new ResponseEntity<>(
                deliveryManagerFacade.getOrdersByStatus(status)
                .subscribeOn(Schedulers.parallel()), HttpStatus.OK
                );

    }


    @ApiOperation(value = "Crear una orden que viene del ecommerce")
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Orden creado", response = OrderDto.class),
            @ApiResponse(code = 500, message = "No creado") })
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

    @ApiOperation(value = "Actualizar una orden en el dominio fulfillment segun una acción a realizar", tags = { "Controlador DeliveryManager" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "deliverymanager creado", response = OrderDto.class),
            @ApiResponse(code = 500, message = "No creado") })
    @PatchMapping("/order/{ecommerceId}")
    public Mono<ResponseEntity<OrderCanonical>> updateStatusOrder(
            @ApiParam(value = "Identificador e-commerce")
            @PathVariable(value = "ecommerceId") String ecommerceId,
            @ApiParam(value = "Accción a realizar de la orden")
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

    @ApiOperation(value = "Obtener los códigos y descripción de cancelación de una orden", tags = { "Controlador DeliveryManager" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Lista obtenida correctamente", response = List.class),
            @ApiResponse(code = 500, message = "No creado") })
    @GetMapping("/cancellation/reason")
    public ResponseEntity<?> getCancellationReasonsCode() {

        log.info("[START] endpoint getCancellationReasonsCode");

        return new ResponseEntity<>(deliveryManagerFacade.getOrderCancellationList(), HttpStatus.OK);
    }

    @ApiOperation(value = "cancelar órdenes que han excedido los días permitidos para entregar o recoger")
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Órdenes canceladas correctamente", response = OrderCancelledCanonical.class),
            @ApiResponse(code = 500, message = "No creado") })
    @PutMapping("/cancellation/orders")
    public Flux<OrderCancelledCanonical> cancelOrderProcess(
            @RequestBody CancellationDto cancellationDto) {
        log.info("[START] endpoint cancelOrderProcess /cancellation/orders - cancellationDto {}",cancellationDto);

       return deliveryManagerFacade.cancelOrderProcess(cancellationDto)
               .doOnComplete(() -> log.info("[END] endpoint cancelOrderProcess /cancellation/orders"));
    }



}