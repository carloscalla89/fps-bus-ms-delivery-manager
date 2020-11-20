package com.inretailpharma.digital.deliverymanager.router;

import com.inretailpharma.digital.deliverymanager.handler.DeliveryHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class DeliveryRouter {

    @Bean
    public RouterFunction<ServerResponse> routeGetScheduleCapacity(DeliveryHandler handler){

        return route(POST("/fulfillment/order"), handler::handleDeliveryOrders);

    }
}
