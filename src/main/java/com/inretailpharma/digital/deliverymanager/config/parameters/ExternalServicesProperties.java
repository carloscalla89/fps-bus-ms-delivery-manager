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

    // properties insink-tracker to DD endpoint
    @Value("${external-service.dispatcher.insink-tracker.uri}")
    private String dispatcherInsinkTrackerUri;

    @Value("${external-service.dispatcher.insink-tracker.uri-mifarma}")
    private String dispatcherInsinkTrackerUriMiFarma;

    @Value("${external-service.dispatcher.insink-tracker.connect-timeout}")
    private String dispatcherInsinkTrackerConnectTimeout;

    @Value("${external-service.dispatcher.insink-tracker.read-timeout}")
    private String dispatcherInsinkTrackerReadTimeout;

    // ------------------------------------------------------

    // properties tracker to DD endpoint
    @Value("${external-service.dispatcher.tracker.uri}")
    private String dispatcherTrackerUri;

    @Value("${external-service.dispatcher.tracker.uri-mifarma}")
    private String dispatcherTrackerUriMifarma;

    @Value("${external-service.dispatcher.tracker.connect-timeout}")
    private String dispatcherTrackerConnectTimeout;

    @Value("${external-service.dispatcher.tracker.read-timeout}")
    private String dispatcherTrackerReadTimeout;

    // ------------------------------------------------------


    // properties legacy-system to DD endpoint
    @Value("${external-service.dispatcher.legacy-system.uri}")
    private String dispatcherLegacySystemUri;

    @Value("${external-service.dispatcher.legacy-system.uri-mifarma}")
    private String dispatcherLegacySystemUriMifarma;

    @Value("${external-service.dispatcher.legacy-system.connect-timeout}")
    private String dispatcherLegacySystemConnectTimeout;

    @Value("${external-service.dispatcher.legacy-system.read-timeout}")
    private String dispatcherLegacySystemReadTimeout;

    // ------------------------------------------------------

    @Value("${external-service.dispatcher.retry-seller-center.uri}")
    private String dispatcherRetrySellerCenterUri;


    // properties to inkatracker lite to update
    @Value("${external-service.inkatrackerlite.update-order.uri}")
    private String inkatrackerLiteUpdateOrderUri;

    @Value("${external-service.inkatrackerlite.update-order.connect-timeout}")
    private String inkatrackerLiteUpdateOrderConnectTimeOut;

    @Value("${external-service.inkatrackerlite.update-order.read-timeout}")
    private String inkatrackerLiteUpdateOrderReadTimeOut;

    // ------------------------------------------------------

    // properties to inkatracker lite to create
    @Value("${external-service.inkatrackerlite.create-order.uri}")
    private String inkatrackerLiteCreateOrderUri;

    @Value("${external-service.inkatrackerlite.create-order.connect-timeout}")
    private String inkatrackerLiteCreateOrderConnectTimeOut;

    @Value("${external-service.inkatrackerlite.create-order.read-timeout}")
    private String inkatrackerLiteCreateOrderReadTimeOut;
    // ------------------------------------------------------


    // properties to inkatracker
    @Value("${external-service.inkatracker.create-order.uri}")
    private String inkatrackerCreateOrderUri;

    @Value("${external-service.inkatracker.create-order.connect-timeout}")
    private String inkatrackerCreateOrderConnectTimeOut;

    @Value("${external-service.inkatracker.create-order.read-timeout}")
    private String inkatrackerCreateOrderReadTimeOut;

    @Value("${external-service.inkatracker.update-status-order.uri}")
    private String inkatrackerUpdateStatusOrderUri;

    @Value("${external-service.inkatracker.update-status-order.connect-timeout}")
    private String inkatrackerUpdateStatusOrderConnectTimeOut;

    @Value("${external-service.inkatracker.update-status-order.read-timeout}")
    private String inkatrackerUpdateOrderReadTimeOut;
    // ------------------------------------------------------

    // ********* properties to inkatracker temporary

    @Value("${external-service.temporary.create-order.uri}")
    private String temporaryCreateOrderUri;

    @Value("${external-service.temporary.create-order.connect-timeout}")
    private String temporaryCreateOrderConnectTimeOut;

    @Value("${external-service.temporary.create-order.read-timeout}")
    private String temporaryCreateOrderReadTimeOut;
    // ------------------------------------------------------


    // properties to order tracker
    @Value("${external-service.order-tracker.create-order.uri}")
    private String orderTrackerCreateOrderUri; 
    
    @Value("${external-service.order-tracker.assign-orders.uri}")
    private String orderTrackerAssignOrdersUri;
    
    @Value("${external-service.order-tracker.unassign-orders.uri}")
    private String orderTrackerUnassignOrdersUri;
    
    @Value("${external-service.order-tracker.update-order-status.uri}")
    private String orderTrackerUpdateOrderStatusUri;
    // ------------------------------------------------------

    // properties fulfillmentcenter
    @Value("${external-service.fulfillment-center.get-center.uri}")
    private String fulfillmentCenterGetCenterUri;

    //product properties
    @Value("${external-service.product.details}")
    private String productDetailsUri;
    @Value("${external-service.product.timeout}")
    private Integer productTimeout;

    @Value("${external-service.fulfillment-center.get-company-center.uri}")
    private String fulfillmentCenterGetCompanyCenterUri;

}
