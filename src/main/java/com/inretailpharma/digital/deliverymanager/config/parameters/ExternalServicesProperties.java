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

    @Value("${external-service.order-tracker.assign-orders.connect-timeout}")
    private String orderTrackerAssignOrdersConnectTimeout;

    @Value("${external-service.order-tracker.assign-orders.read-timeout}")
    private String orderTrackerAssignOrdersReadTimeout;

    @Value("${external-service.order-tracker.unassign-orders.uri}")
    private String orderTrackerUnassignOrdersUri;

    @Value("${external-service.order-tracker.unassign-orders.connect-timeout}")
    private String orderTrackerUnassignOrdersConnectTimeout;

    @Value("${external-service.order-tracker.unassign-orders.read-timeout}")
    private String orderTrackerUnassignOrdersReadTimeout;

    @Value("${external-service.order-tracker.update-order-status.uri}")
    private String orderTrackerUpdateOrderStatusUri;

    @Value("${external-service.order-tracker.update-partial.uri}")
    private String orderTrackerUpdatePartialUri;

    @Value("${external-service.order-tracker.update-partial.connect-timeout}")
    private String orderTrackerUpdatePartialConnectTimeout;

    @Value("${external-service.order-tracker.update-partial.read-timeout}")
    private String orderTrackerUpdatePartialReadTimeout;
    // ------------------------------------------------------

    // properties fulfillmentcenter
    @Value("${external-service.fulfillment-center.get-center.uri}")
    private String fulfillmentCenterGetCenterUri;

    @Value("${external-service.fulfillment-center.get-center.connect-timeout}")
    private String fulfillmentCenterGetCenterConnectTimeOut;

    @Value("${external-service.fulfillment-center.get-center.read-timeout}")
    private String fulfillmentCenterGetCenterReadTimeOut;

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

    // endoint para actualizar una orden (en linea - bbr)
    @Value("${external-service.liquidation.update-order-online.uri}")
    private String liquidationUpdateOrderOnlineUri;

    @Value("${external-service.liquidation.update-order-online.connect-timeout}")
    private String liquidationUpdateOrderOnlineConnectTimeOut;

    @Value("${external-service.liquidation.update-order-online.read-timeout}")
    private String liquidationUpdateOrderOnlineReadTimeOut;

    // ********************************************

    // endoint para traer la orden desde el insink(call center)
    @Value("${external-service.insink.get-order-callcenter.uri}")
    private String insinkGetOrderCallcenterUri;

    @Value("${external-service.insink.get-order-callcenter.connect-timeout}")
    private String insinkGetOrderCallcenterConnectTimeout;

    @Value("${external-service.insink.get-order-callcenter.read-timeout}")
    private String insinkGetOrderCallcenterReadTimeout;
    
    // endpoint para legacy bridge
    @Value("${external-service.legacy-bridge.release-stock.uri}")
    private String legacyBridgeReleaseStockUri;

    @Value("${external-service.legacy-bridge.release-stock.connect-timeout}")
    private String legacyBridgeReleaseStockConnectTimeout;

    @Value("${external-service.legacy-bridge.release-stock.read-timeout}")
    private String legacyBridgeReleaseStockReadTimeout;
    
    // endpoint para ruteo
    @Value("${external-service.routing.create-order.uri}")
    private String routingCreateOrderUri;

    @Value("${external-service.routing.create-order.connect-timeout}")
    private String routingCreateOrderConnectTimeout;

    @Value("${external-service.routing.create-order.read-timeout}")
    private String routingCreateOrderReadTimeout;
    
    @Value("${external-service.routing.create-token.uri}")
    private String routingCreateTokenUri;

    @Value("${external-service.routing.create-token.connect-timeout}")
    private String routingCreateTokenConnectTimeout;

    @Value("${external-service.routing.create-token.read-timeout}")
    private String routingCreateTokenReadTimeout;
    
    @Value("${external-service.routing.cancel-order.uri}")
    private String routingCancelOrderUri;

    @Value("${external-service.routing.cancel-order.connect-timeout}")
    private String routingCancelOrderConnectTimeout;

    @Value("${external-service.routing.cancel-order.read-timeout}")
    private String routingCancelOrderReadTimeout;
    
    // endpoint productos
    @Value("${external-service.product.get-dimensions.uri}")
    private String productGetDimensionsUri;

    @Value("${external-service.product.get-dimensions.connect-timeout}")
    private String productGetDimensionsConnectTimeout;

    @Value("${external-service.product.get-dimensions.read-timeout}")
    private String productGetDimensionsReadTimeout;


}
