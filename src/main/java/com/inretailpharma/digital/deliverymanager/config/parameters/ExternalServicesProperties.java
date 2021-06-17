package com.inretailpharma.digital.deliverymanager.config.parameters;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "external-service")
public class ExternalServicesProperties {

    // properties audit
    @Value("${external-service.audit.create-order}")
    private String uriApiService;

    @Value("${external-service.audit.create-order-history-audit}")
    private String uriCreateHistoryAuditApiService;

    @Value("${external-service.audit.update-order-history-audit}")
    private String uriUpdateHistoryAuditApiService;

    @Value("${external-service.dispatcher.tracker.connect-timeout}")
    private String auditConnectTimeout;

    @Value("${external-service.dispatcher.tracker.read-timeout}")
    private String auditReadTimeout;



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

    // properties order/ecommerce/{orderId} to DD endpoint
    @Value("${external-service.dispatcher.order-ecommerce.uri}")
    private String dispatcherOrderEcommerceUri;

    @Value("${external-service.dispatcher.order-ecommerce.uri-mifarma}")
    private String dispatcherOrderEcommerceUriMifarma;

    @Value("${external-service.dispatcher.order-ecommerce.connect-timeout}")
    private String dispatcherOrderEcommerceConnectTimeout;

    @Value("${external-service.dispatcher.order-ecommerce.read-timeout}")
    private String dispatcherOrderEcommerceReadTimeout;

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

    @Value("${external-service.fulfillment-center.get-center.connect-timeout}")
    private String fulfillmentCenterGetCenterConnectTimeOut;

    @Value("${external-service.fulfillment-center.get-center.read-timeout}")
    private String fulfillmentCenterGetCenterReadTimeOut;

    // properties seller-center
    @Value("${external-service.seller-center.add-controversy.uri}")
    private String addControversyUri;
    
    //online payment    
    //Liquidation
    @Value("${external-service.online-payment.liquidated.uri}")
    private String onlinePaymentLiquidatedUri;    
    
    @Value("${external-service.online-payment.liquidated.uri-mifa}")
    private String onlinePaymentLiquidatedUriMifa;

    @Value("${external-service.online-payment.liquidated.connect-timeout}")
    private String onlinePaymentLiquidatedConnectTimeOut;

    @Value("${external-service.online-payment.liquidated.read-timeout}")
    private String onlinePaymentLiquidatedReadTimeOut;
    
    //Cancelation
    @Value("${external-service.online-payment.rejected.uri}")
    private String onlinePaymentRejectedUri;
    
    @Value("${external-service.online-payment.rejected.uri-mifa}")
    private String onlinePaymentRejectedUriMifa;

    @Value("${external-service.online-payment.rejected.connect-timeout}")
    private String onlinePaymentRejectedConnectTimeOut;

    @Value("${external-service.online-payment.rejected.read-timeout}")
    private String onlinePaymentRejectedReadTimeOut;


    // properties lambda notification
    @Value("${external-service.notification-lambda.send-message.uri}")
    private String notificationLambdaUri;

    @Value("${external-service.notification-lambda.send-message.connect-timeout}")
    private String notificationLambdaConnectTimeOut;

    @Value("${external-service.notification-lambda.send-message.read-timeout}")
    private String notificationLambdaReadTimeOut;

    @Value("${api.insink.stock}")
    private String uriApiRestoreStock;



    //********** properties para el componente de liquidación

    // endoint para create una orden
    @Value("${external-service.liquidation.create-order.uri}")
    private String liquidationCreateOrderUri;

    @Value("${external-service.liquidation.create-order.connect-timeout}")
    private String liquidationCreateOrderConnectTimeOut;

    @Value("${external-service.liquidation.create-order.read-timeout}")
    private String liquidationCreateOrderReadTimeOut;


    // endoint para create una orden
    @Value("${external-service.liquidation.update-order.uri}")
    private String liquidationUpdateOrderUri;

    @Value("${external-service.liquidation.update-order.connect-timeout}")
    private String liquidationUpdateOrderConnectTimeOut;

    @Value("${external-service.liquidation.update-order.read-timeout}")
    private String liquidationUpdateOrderReadTimeOut;

    // ********************************************
}
