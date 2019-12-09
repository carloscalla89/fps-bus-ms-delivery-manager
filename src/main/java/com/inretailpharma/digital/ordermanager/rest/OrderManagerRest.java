package com.inretailpharma.digital.ordermanager.rest;

import com.inretailpharma.digital.ordermanager.dto.OrderDto;
import com.inretailpharma.digital.ordermanager.facade.OrderProcessFacade;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/OrderManager")
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

        orderProcessFacade.updateStatusOrder(orderDto);

        log.info("finish sending to Kafka");
        return new ResponseEntity<>("success", HttpStatus.CREATED);
     }

}