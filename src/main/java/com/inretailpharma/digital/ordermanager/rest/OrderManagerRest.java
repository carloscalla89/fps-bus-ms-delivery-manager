package com.inretailpharma.digital.ordermanager.rest;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.facade.OrderProcessFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/fulfillment")
@Api(value = "OrderManagerRest", produces = "application/json")
/**
 * Controlador de commands
 *
 * @author
 */
@Slf4j
public class  OrderManagerRest {

    private OrderProcessFacade orderProcessFacade;

    public OrderManagerRest(OrderProcessFacade orderProcessFacade) {
        this.orderProcessFacade = orderProcessFacade;
    }

    @ApiOperation(value = "Crea un ordermanager", tags = { "Controlador OrderManagers" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "ordermanager creado", response = OrderDto.class), @ApiResponse(code = 500, message = "No creado") })
    @PostMapping("/messages")
    public ResponseEntity<String> create(@RequestBody OrderDto orderDto) {



        log.info("finish sending to Kafka");
        return new ResponseEntity<>("success", HttpStatus.CREATED);
     }


    @ApiOperation(value = "Crea una orden en el entonor fulfillment", tags = { "Controlador OrderManagers" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "ordermanager creado", response = OrderDto.class), @ApiResponse(code = 500, message = "No creado") })
    @PostMapping("/order")
     public ResponseEntity<?> createOrder(@RequestBody OrderDto orderDto) {

        return new ResponseEntity<>(orderProcessFacade.createOrder(orderDto), HttpStatus.CREATED);
     }

    @ApiOperation(value = "Listar ordenes con error", tags = { "Controlador OrderManagers" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "ordermanager creado", response = OrderDto.class),
            @ApiResponse(code = 500, message = "Error in server ot obtain list") })
    @GetMapping("/order")
    public ResponseEntity<?> getOrderListWithError() {

        return new ResponseEntity<>(orderProcessFacade.getListOrdersByStatusError(), HttpStatus.OK);
    }

}