package com.inretailpharma.digital.OrderManager.rest;

import com.inretailpharma.digital.OrderManager.dto.OrderDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

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

    @Autowired
    KafkaTemplate<String, OrderDto> kafkaTemplate;

    @Value("${spring.kafka.producer.topic}")
    private String topic;


    @ApiOperation(value = "Crea un OrderManager", tags = { "Controlador OrderManagers" })
    @ApiResponses(value = { //
            @ApiResponse(code = 200, message = "OrderManager creado", response = OrderDto.class), @ApiResponse(code = 500, message = "No creado") })
    @PostMapping("/messages")
    public ResponseEntity<String> create(@RequestBody OrderDto msg) {

        log.info("Enviando a Kafka objeto {} ",msg);

        ListenableFuture<SendResult<String , OrderDto>> future = kafkaTemplate.send(topic, msg);

        future.addCallback(new ListenableFutureCallback<SendResult<String, OrderDto>>() {

            @Override
            public void onSuccess(SendResult<String, OrderDto> result) {

                log.info("On success:"+result);
            }

            @Override
            public void onFailure(Throwable ex) {
                ex.printStackTrace();
                log.error("error:"+ex);
            }

        });
        log.info("finish sending to Kafka");
        return new ResponseEntity<>("success", HttpStatus.CREATED);
     }

}