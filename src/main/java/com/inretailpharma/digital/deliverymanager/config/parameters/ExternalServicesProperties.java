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

    @Value("${external-service.dispatcher.insink-tracker.uri-mifarma}")
    private String dispatcherInsinkTrackerUriMiFarma;

    @Value("${external-service.dispatcher.insink-tracker.connect-timeout}")
    private String dispatcherInsinkTrackerConnectTimeout;

    @Value("${external-service.dispatcher.insink-tracker.read-timeout}")
    private String dispatcherInsinkTrackerReadTimeout;


    // properties to DD endpoint tracker inka
    @Value("${external-service.dispatcher.tracker.uri}")
    private String dispatcherTrackerUri;

    // properties to DD endpoint mifarma
    @Value("${external-service.dispatcher.tracker.uri-mifarma}")
    private String dispatcherTrackerUriMifarma;

    @Value("${external-service.dispatcher.tracker.connect-timeout}")
    private String dispatcherTrackerConnectTimeout;

    @Value("${external-service.dispatcher.tracker.read-timeout}")
    private String dispatcherTrackerReadTimeout;

    // properties to inkatracker lite
    @Value("${external-service.inkatrackerlite.update-order.uri}")
    private String inkatrackerLiteUpdateOrderUri;

    @Value("${external-service.inkatrackerlite.update-order.connect-timeout}")
    private String inkatrackerLiteUpdateOrderConnectTimeOut;

    @Value("${external-service.inkatrackerlite.update-order.read-timeout}")
    private String inkatrackerLiteUpdateOrderReadTimeOut;

    // properties to inkatracker
    @Value("${external-service.inkatracker.update-status-order.uri}")
    private String inkatrackerUpdateOrderUri;

    @Value("${external-service.inkatracker.update-status-order.connect-timeout}")
    private String inkatrackerUpdateOrderConnectTimeOut;

    @Value("${external-service.inkatracker.update-status-order.read-timeout}")
    private String inkatrackerUpdateOrderReadTimeOut;

    // properties to order tracker
    @Value("${external-service.order-tracker.create-order.uri}")
    private String orderTrackerCreateOrderUri;
    
    @Value("${external-service.order-tracker.assign-orders.uri}")
    private String orderTrackerAssignOrdersUri;
    
    @Value("${external-service.order-tracker.unassign-orders.uri}")
    private String orderTrackerUnassignOrdersUri;
    
    @Value("${external-service.fulfillment-center.get-center.uri}")
    private String fulfillmentCenterGetCenterUri;
}
