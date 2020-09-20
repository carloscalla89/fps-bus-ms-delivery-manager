package com.inretailpharma.digital.deliverymanager.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractValidationHandler<T, U extends Validator> {

    private final Class<T> validationClass;

    private final U validator;

    protected AbstractValidationHandler(Class<T> clazz, U validator) {
        this.validationClass = clazz;
        this.validator = validator;
    }

    abstract protected Mono<ServerResponse> processDelivery(T validBody, final ServerRequest originalRequest);

    public final Mono<ServerResponse> handleDeliveryOrders(final ServerRequest request) {
        return request
                .bodyToMono(this.validationClass)
                .flatMap(body -> {

                    Errors errors = new BeanPropertyBindingResult(body, this.validationClass.getName());
                    this.validator.validate(body, errors);

                    if (errors == null || errors.getAllErrors()
                            .isEmpty()) {
                        return processDelivery(body, request);
                    } else {
                        return onValidationErrors(errors);
                    }
                });
    }

    protected Mono<ServerResponse> onValidationErrors(Errors errors) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                errors.getAllErrors()
                        .stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(",")));
    }
}
