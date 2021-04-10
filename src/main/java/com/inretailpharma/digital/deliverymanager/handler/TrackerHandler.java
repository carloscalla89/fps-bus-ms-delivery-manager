package com.inretailpharma.digital.deliverymanager.handler;

import com.inretailpharma.digital.deliverymanager.canonical.manager.OrderCanonical;
import com.inretailpharma.digital.deliverymanager.dto.OrderDto;
import com.inretailpharma.digital.deliverymanager.facade.TrackerFacade;
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
public class TrackerHandler extends AbstractValidationHandler<OrderDto, CustomRequestEntityValidator> {

    private TrackerFacade trackerFacade;

    protected TrackerHandler(TrackerFacade trackerFacade) {

        super(OrderDto.class  , new CustomRequestEntityValidator());
        this.trackerFacade = trackerFacade;
    }

    @Override
    protected Mono<ServerResponse> processDelivery(OrderDto validBody, ServerRequest originalRequest) {
        return null;
    }

    @Override
    protected Mono<ServerResponse> processTracker(ServerRequest request) {
        Long ecommerceId = Long.parseLong(request.pathVariable("ecommerceId"));

        return trackerFacade
                    .getOrderByEcommerceId(ecommerceId)
                    .flatMap(response -> ServerResponse
                                            .ok()
                                            .contentType(MediaType.APPLICATION_STREAM_JSON)
                                            .bodyValue(response)

                    )
                    .switchIfEmpty(ServerResponse.noContent().build())
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
    protected Mono<ServerResponse> processTrackers(ServerRequest request) {
        String ecommerceIds = request.queryParam("ecommerceIds").get();

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(trackerFacade.getOrderByEcommerceIds(ecommerceIds), OrderCanonical.class)
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
}
