package com.inretailpharma.digital.deliverymanager.errorhandling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes,
                                          ResourceProperties resourceProperties,
                                          ApplicationContext applicationContext,
                                          ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, resourceProperties, applicationContext);
        this.setMessageWriters(serverCodecConfigurer.getWriters());
        this.setMessageReaders(serverCodecConfigurer.getReaders());

    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        log.info("renderErrorResponse");


        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, false);

        HttpStatus status = HttpStatus.valueOf(Integer.valueOf(errorPropertiesMap.get("status").toString()));

        ServerResponseError serverResponseError = new ServerResponseError();
        serverResponseError.setStatusCode(status.value());

        Optional.ofNullable(errorPropertiesMap.get("message"))
                .ifPresent(r -> serverResponseError.setErrors(Arrays.stream(r.toString().split(",")).collect(Collectors.toList())));



        serverResponseError.setPath(errorPropertiesMap.get("path").toString());
        serverResponseError.setErrorType(errorPropertiesMap.get("error").toString());

        log.error("serverResponseError:{}",serverResponseError);

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromValue(serverResponseError));
    }

}
