package com.inretailpharma.digital.deliverymanager.rest;

import com.inretailpharma.digital.deliverymanager.dto.ActionDto;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.facade.OrderProcessFacade;
import io.swagger.annotations.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/fulfillment")
@Api(value = "OrderManagerRest", produces = "application/json")
/**
 * Controlador de commands
 *
 * @author
 */
@Slf4j
public class DeliveryManagerRest {

    private OrderProcessFacade orderProcessFacade;

    public DeliveryManagerRest(OrderProcessFacade orderProcessFacade) {
        this.orderProcessFacade = orderProcessFacade;
    }

    @ApiOperation(value = "Crea un deliverymanager", tags = { "Controlador OrderManagers" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "deliverymanager creado", response = OrderDto.class), @ApiResponse(code = 500, message = "No creado") })
    @PostMapping("/messages")
    public ResponseEntity<String> create(@RequestBody OrderDto orderDto) {

        log.info("finish sending to Kafka");
        return new ResponseEntity<>("success", HttpStatus.CREATED);
     }


    @ApiOperation(value = "Ejecutar accion para actualizar una orden", tags = { "Controlador OrderManagers" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "deliverymanager creado", response = OrderDto.class),
            @ApiResponse(code = 500, message = "No creado") })
    @PostMapping("/order")
     public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto) {
        log.info("[START] endpoint /fulfillment/order - orderDto:{}",orderDto);
        return new ResponseEntity<>(orderProcessFacade.createOrder(orderDto), HttpStatus.CREATED);
     }

    @ApiOperation(value = "Actualizar una orden en el dominio fulfillment segun una acción a realizar", tags = { "Controlador DeliveryManager" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "deliverymanager creado", response = OrderDto.class),
            @ApiResponse(code = 500, message = "No creado") })
    @PatchMapping("/order/{ecommerceId}")
    public ResponseEntity<?> updateStatusOrder(
            @ApiParam(value = "Identificador e-commerce")
            @PathVariable(value = "ecommerceId") String ecommerceId,
            @ApiParam(value = "Accción a realizar de la orden")
            @RequestBody ActionDto action) {

        log.info("[START] endpoint updateStatus /order/{ecommerceId} - ecommerceId {} - action {}"
                ,ecommerceId,action);

        return new ResponseEntity<>(orderProcessFacade.getUpdateOrder(action, ecommerceId), HttpStatus.OK);
    }


    @ApiOperation(value = "Obtener los códigos y descripción de cancelación de una orden", tags = { "Controlador DeliveryManager" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "Lista obtenida correctamente", response = List.class),
            @ApiResponse(code = 500, message = "No creado") })
    @GetMapping("/cancellation/reason")
    public ResponseEntity<?> getCancellationReasonsCode() {

        log.info("[START] endpoint getCancellationReasonsCode");

        return new ResponseEntity<>(orderProcessFacade.getOrderCancellationList(), HttpStatus.OK);
    }

}