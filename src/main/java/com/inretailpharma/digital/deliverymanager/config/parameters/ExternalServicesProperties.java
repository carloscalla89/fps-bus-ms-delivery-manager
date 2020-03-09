package com.inretailpharma.digital.deliverymanager.config.parameters;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "external-service")
public class ExternalServicesProperties {


    @Value("${external-service.audit.create-order}")
    private String uriApiService;

    @Value("${external-service.audit.time-out}")
    private Integer timeout;

    @Value("${external-service.dispatcher.insink-tracker.uri}")
    private String dispatcherInsinkTrackerUri;

    @Value("${external-service.dispatcher.insink-tracker.connect-timeout}")
    private String dispatcherInsinkTrackerConnectTimeout;

    @Value("${external-service.dispatcher.insink-tracker.read-timeout}")
    private String dispatcherInsinkTrackerReadTimeout;

    @Value("${external-service.dispatcher.tracker.uri}")
    private String dispatcherTrackerUri;

    @Value("${external-service.dispatcher.tracker.connect-timeout}")
    private String dispatcherTrackerConnectTimeout;

    @Value("${external-service.dispatcher.tracker.read-timeout}")
    private String dispatcherTrackerReadTimeout;

    @Value("${external-service.inkatrackerlite.update-order.uri}")
    private String inkatrackerLiteUpdateOrderUri;

    @Value("${external-service.inkatrackerlite.update-order.connect-timeout}")
    private String inkatrackerLiteUpdateOrderConnectTimeOut;

    @Value("${external-service.inkatrackerlite.update-order.read-timeout}")
    private String inkatrackerLiteUpdateOrderReadTimeOut;

    @Value("${external-service.order-tracker.create-order.uri}")
    private String orderTrackerCreateOrderUri;

    @Value("${external-service.order-tracker.create-order.connect-timeout}")
    private String orderTrackerCreateOrderConnectTimeOut;

    @Value("${external-service.order-tracker.create-order.read-timeout}")
    private String orderTrackerCreateOrderReadTimeOut;

}
