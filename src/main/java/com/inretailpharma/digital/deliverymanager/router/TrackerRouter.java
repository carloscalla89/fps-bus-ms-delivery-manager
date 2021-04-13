package com.inretailpharma.digital.deliverymanager.router;

import com.inretailpharma.digital.deliverymanager.handler.TrackerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
public class TrackerRouter {

    @Bean
    public RouterFunction<ServerResponse> routeGetOrder(TrackerHandler handler){

        return route(GET("/fulfillment/tracker/orders/{ecommerceId}"), handler::handleTrackerOrders);

    }

    @Bean
    public RouterFunction<ServerResponse> routeGetOrders(TrackerHandler handler){

        return route(GET("/fulfillment/tracker/orders"), handler::handleTrackersOrders);

    }
}
