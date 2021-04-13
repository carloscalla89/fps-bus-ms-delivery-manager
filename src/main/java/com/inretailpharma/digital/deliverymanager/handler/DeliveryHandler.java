package com.inretailpharma.digital.deliverymanager.handler;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.facade.DeliveryManagerFacade;
import com.inretailpharma.digital.deliverymanager.validation.AbstractValidationHandler;
import com.inretailpharma.digital.deliverymanager.validation.CustomRequestEntityValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class DeliveryHandler extends AbstractValidationHandler<OrderDto, CustomRequestEntityValidator> {

    private DeliveryManagerFacade deliveryManagerFacade;

    protected DeliveryHandler(DeliveryManagerFacade deliveryManagerFacade) {

        super(OrderDto.class  , new CustomRequestEntityValidator());
        this.deliveryManagerFacade = deliveryManagerFacade;
    }

    @Override
    protected Mono<ServerResponse> processDelivery(OrderDto validBody, ServerRequest originalRequest) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_STREAM_JSON)
                .body(deliveryManagerFacade.createOrder(validBody), OrderCanonical.class)
                .onErrorResume(e -> {

                    e.printStackTrace();
                    log.info("Error e:{}",e.getMessage());

                    if (e instanceof ResponseStatusException) {
                        return Mono.error(e);
                    } else {
                        return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
                    }

                });
    }

    @Override
    protected Mono<ServerResponse> processTracker(ServerRequest originalRequest) {
        return null;
    }

    @Override
    protected Mono<ServerResponse> processTrackers(ServerRequest originalRequest) {
        return null;
    }


}
