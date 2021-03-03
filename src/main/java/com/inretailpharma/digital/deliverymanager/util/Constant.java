package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.entity.PaymentMethod;
import com.inretailpharma.digital.deliverymanager.proxy.InkatrackerLiteServiceImpl;
import com.inretailpharma.digital.deliverymanager.proxy.InkatrackerServiceImpl;
import org.apache.commons.lang3.EnumUtils;

import java.util.Optional;

public interface Constant {

    enum CancellationStockDispatcher {

        CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK("C05", "Producto no disponible en el Delivery Center"),
        CANCELLED_ORDER_NOT_ENOUGH_STOCK("C05", "Producto no disponible en el Delivery Center"),
        NONE(null, null);

        private String id;
        private String reason;

        CancellationStockDispatcher(String id, String reason) {
            this.id = id;
            this.reason = reason;
        }

        public static CancellationStockDispatcher getByName(String name) {

            return EnumUtils.getEnumList(CancellationStockDispatcher.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(NONE);
        }

        public static String getDetailCancelStock(String name, String observation, String detail) {

            return EnumUtils.getEnumList(CancellationStockDispatcher.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().map(d -> detail)
                    .orElse(observation);
        }

        public String getId() {
            return id;
        }

        public String getReason() {
            return reason;
        }
    }

    enum StatusDispatcherResult {

        INVALID_STRUCTURE("ERROR_INSERT_INKAVENTA"), ORDER_RESERVED("SUCCESS_RESERVED_ORDER"),
        ORDER_REGISTERED("CONFIRMED"), NOT_ENOUGH_STOCK("CANCELLED_ORDER_NOT_ENOUGH_STOCK"),
        NOT_ENOUGH_STOCK_PAYMENT_ONLINE("CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK"),
        ORDER_FAILED("ERROR_INSERT_INKAVENTA"), NONE("ERROR_INSERT_INKAVENTA");

        private String status;

        StatusDispatcherResult(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public static StatusDispatcherResult getByName(String name) {

            return EnumUtils.getEnumList(StatusDispatcherResult.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(NONE);
        }
    }

    enum DispatcherImplementation {
        IKF("deliveryDispatcherInka"), MF("deliveryDispatcherMifa");

        private String name;

        DispatcherImplementation(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static DispatcherImplementation getByCompanyCode(String companyCode) {

            return EnumUtils.getEnumList(DispatcherImplementation.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(companyCode)).findFirst().orElse(IKF);
        }

    }

    enum TrackerImplementation {
        inkatrackerlite(4, InkatrackerLiteServiceImpl.class, "DRUGSTORE"),
        inkatracker(3, InkatrackerServiceImpl.class,"DELIVERY_CENTER"),
        NONE(4, InkatrackerLiteServiceImpl.class, "DRUGSTORE");

        private Integer id;
        private Class trackerImplement;
        private String localType;

        TrackerImplementation(Integer id, Class trackerImplement, String localType) {
            this.id = id;
            this.trackerImplement = trackerImplement;
            this.localType = localType;
        }

        public Integer getId() {
            return id;
        }

        public Class getTrackerImplement() {
            return trackerImplement;
        }

        public String getLocalType() {
            return localType;
        }

        public static TrackerImplementation getIdByClassImplement(String classImplement) {

            return EnumUtils.getEnumList(TrackerImplementation.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(classImplement)).findFirst().orElse(NONE);
        }

    }

    interface OrderTrackerResponseCode {
        String SUCCESS_CODE = "0";
        String ERROR_CODE = "1";
        String EMPTY_CODE = "2";
        String ASSIGN_SUCCESS_CODE = "A0";
        String ASSIGN_PARTIAL_CODE = "A1";
        String ASSIGN_ERROR_CODE = "A2";
    }

    interface ApplicationsParameters {
        String ACTIVATED_AUDIT_VALUE = "1";
        String ACTIVATED_AUDIT = "ACTIVATED_AUDIT";

        String DAYS_PICKUP_MAX_RET = "DAYS_PICKUP_MAX_RET";
        String ACTIVATED_DD_IKF = "ACTIVATED_DD_IKF";
        String ACTIVATED_DD_MF = "ACTIVATED_DD_MF";
        String ACTIVATED_DD_ = "ACTIVATED_DD_";

        String ACTIVATED_SEND_ = "ACTIVATED_SEND_";
    }

    interface InsinkErrorCode {
        String CODE_ERROR_STOCK = "E-1";
    }

    enum Source {
        SC
    }

    String DEFAULT_DS = "RAD";
    int DEFAULT_SC_CARD_PROVIDER_ID = 1;
    int DEFAULT_SC_PAYMENT_METHOD_ID = 3;
    String DEFAULT_SC_PAYMENT_METHOD_VALUE = "Pago en línea";

    interface Receipt {
        String TICKET = "TICKET";
        String INVOICE = "INVOICE";
    }

    interface OnlinePayment {
        String LIQUIDETED = "LIQUIDETED";
    }

    enum ActionOrder {

        ATTEMPT_TRACKER_CREATE(1, "reintento para enviar la orden a un tracker"),
        ON_STORE_ORDER(4, "actualizar el BILLING ID(número de pedido diario) a un tracker"),

        ATTEMPT_INSINK_CREATE(2, "reintento para enviar la órden al insink"),

        REJECT_ORDER(4, "Acción para cambiar el estado de la orden como cancelada"),
        CANCEL_ORDER(4, "Acción para cambiar el estado de la orden como cancelada"),
        DELIVER_ORDER(4, "Acción para cambiar el estado de la orden como entregada"),
        READY_PICKUP_ORDER(4, "Acción para cambiar el estado de la orden como lista para recoger"),
        INVOICED_ORDER(4, "Acción para cambiar el estado de la orden a facturada"),

        // ========== nuevas actions que enviarán TI - 29-10-2020
        // =========================
        READY_FOR_BILLING(4, "Accion para cambiar el estado de la orden a READY_FOR_BILLING"),
        PICK_ORDER(4, "Acción para cambiar el estado de la orden a PICKEADO"),
        PREPARE_ORDER(4, "Acción para cambiar el estado de la orden a PREPADO"),
        // =================================================================================


        // =========== nuevos actions que se enviarán desde el order-tracker
        ASSIGN_ORDER(4, "Acción para asignar órdenes"),
        UNASSIGN_ORDER(4, "Acción para asignar órdenes"),
        ON_ROUTE_ORDER(4, "Acción para CAMBIAR  al estado ON_ROUTE"),
        ARRIVAL_ORDER(4, "Acción para asignar al estado ARRIVED"),

        LIQUIDATED_ONLINE_PAYMENT(6, "Acción para informar la liquidacion del pago"),

        FILL_ORDER(5, "Accion para llenar data del ecommerce a una orden"),

        NONE(0, "Not found status");



        private Integer code;
        private String description;

        ActionOrder(Integer code, String description) {
            this.code = code;
            this.description = description;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public static ActionOrder getByName(String name) {

            return EnumUtils.getEnumList(ActionOrder.class).stream().filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst().orElse(NONE);
        }
    }
    enum OrderStatusTracker {

        ON_STORE_ORDER("ON_STORE_ORDER", "ON_STORE", OrderStatus.CONFIRMED_TRACKER, OrderStatus.ERROR_INSERT_TRACKER,
                ActionOrder.ON_STORE_ORDER.name()),

        CANCELLED_ORDER("CANCELLED", "CANCELLED", OrderStatus.CANCELLED_ORDER, OrderStatus.ERROR_CANCELLED,
                ActionOrder.CANCEL_ORDER.name()),

        CANCELLED_ORDER_ONLINE_PAYMENT("CANCELLED", "CANCELLED", OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT,
                OrderStatus.ERROR_CANCELLED, ActionOrder.CANCEL_ORDER.name()),

        CANCELLED_ORDER_NOT_ENOUGH_STOCK("CANCELLED", "CANCELLED", OrderStatus.CANCELLED_ORDER_NOT_ENOUGH_STOCK,
                OrderStatus.ERROR_CANCELLED, ActionOrder.CANCEL_ORDER.name()),

        CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK("CANCELLED", "CANCELLED",
                OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK, OrderStatus.ERROR_CANCELLED,
                ActionOrder.CANCEL_ORDER.name()),

        REJECTED_ORDER("REJECTED", "REJECTED", OrderStatus.CANCELLED_ORDER, OrderStatus.ERROR_REJECTED,
                ActionOrder.REJECT_ORDER.name()),

        REJECTED_ORDER_ONLINE_PAYMENT("REJECTED", "REJECTED", OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT,
                OrderStatus.ERROR_REJECTED, ActionOrder.REJECT_ORDER.name()),

        REJECTED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK("REJECTED", "REJECTED",
                OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK, OrderStatus.ERROR_REJECTED,
                ActionOrder.REJECT_ORDER.name()),

        ERROR_INSERT_TRACKER("CONFIRMED", "CONFIRMED", OrderStatus.ERROR_INSERT_TRACKER,
                OrderStatus.ERROR_INSERT_TRACKER, ActionOrder.ATTEMPT_TRACKER_CREATE.name()),

        ERROR_RESERVED_ORDER("CONFIRMED", "CONFIRMED", OrderStatus.ERROR_INSERT_TRACKER,
                OrderStatus.ERROR_INSERT_INKAVENTA, ActionOrder.NONE.name()),

        CONFIRMED("CONFIRMED", "CONFIRMED", OrderStatus.CONFIRMED, OrderStatus.ERROR_INSERT_INKAVENTA,
                ActionOrder.NONE.name()),

        CONFIRMED_TRACKER("CONFIRMED", "CONFIRMED", OrderStatus.CONFIRMED_TRACKER, OrderStatus.ERROR_INSERT_INKAVENTA,
                ActionOrder.NONE.name()),

        SUCCESS_RESERVED_ORDER("CONFIRMED", "CONFIRMED", OrderStatus.CONFIRMED_TRACKER,
                OrderStatus.ERROR_INSERT_INKAVENTA, ActionOrder.NONE.name()),

        DELIVERED_ORDER("DELIVER_ORDER", "DELIVERED", OrderStatus.DELIVERED_ORDER, OrderStatus.ERROR_DELIVERED,
                ActionOrder.DELIVER_ORDER.name()),

        NOT_FOUND_ACTION("NOT_FOUND_ACTION", "NOT_FOUND_ACTION", OrderStatus.NOT_FOUND_CODE, OrderStatus.NOT_FOUND_CODE,
                ActionOrder.NONE.name()),

        INVOICED_ORDER("INVOICED_ORDER", "INVOICED", OrderStatus.INVOICED, OrderStatus.ERROR_INVOICED,
                ActionOrder.INVOICED_ORDER.name()),

        PICKED_ORDER("PICKING_ORDER", "PICKING", OrderStatus.PICKED_ORDER, OrderStatus.ERROR_PICKED,
                ActionOrder.PICK_ORDER.name()),

        PREPARED_ORDER("PREPARED_ORDER", "PREPARED", OrderStatus.PREPARED_ORDER, OrderStatus.ERROR_PREPARED,
                ActionOrder.PREPARE_ORDER.name()),

        READY_FOR_BILLING("READY_FOR_BILLING", "READY_FOR_BILLING", OrderStatus.READY_PICKUP_ORDER,
                OrderStatus.ERROR_READY_FOR_PICKUP, ActionOrder.READY_FOR_BILLING.name()),

        READY_PICKUP_ORDER("READY_FOR_PICKUP", "READY_FOR_PICKUP", OrderStatus.READY_PICKUP_ORDER,
                OrderStatus.ERROR_READY_FOR_PICKUP, ActionOrder.READY_PICKUP_ORDER.name()),

        // =============================== nuevos estados 02-02-21
        ASSIGNED_ORDER("ASSIGNED", "ASSIGNED", OrderStatus.ASSIGNED,
                OrderStatus.ERROR_ASSIGNED, ActionOrder.ASSIGN_ORDER.name()),

        ON_ROUTE_ORDER("ON_ROUTE", "ON_ROUTE", OrderStatus.ON_ROUTED_ORDER,
                OrderStatus.ERROR_ON_ROUTED, ActionOrder.ON_ROUTE_ORDER.name()),

        ARRIVED_ORDER("ARRIVED", "ARRIVED", OrderStatus.ARRIVED_ORDER,
                OrderStatus.ERROR_ARRIVED, ActionOrder.ARRIVAL_ORDER.name());


        private String trackerStatus;
        private String trackerLiteStatus;
        private OrderStatus orderStatus;
        private OrderStatus orderStatusError;
        private String actionName;

        public static OrderStatusTracker getByName(String name) {
            return EnumUtils.getEnumList(OrderStatusTracker.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(NOT_FOUND_ACTION);
        }

        public static OrderStatus getOrderStatusByTrackerStatus(String trackerStatus, String paymentMethod) {

            OrderStatus orderStatus = OrderStatus.CANCELLED_ORDER;

            switch (trackerStatus) {
                case "CANCELLED":

                    if (paymentMethod.equalsIgnoreCase(PaymentMethod.PaymentType.ONLINE_PAYMENT.name())) {

                        orderStatus = OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT;

                    }

                    break;
                case "REJECTED":
                    if (paymentMethod.equalsIgnoreCase(PaymentMethod.PaymentType.ONLINE_PAYMENT.name())) {

                        orderStatus = OrderStatus.REJECTED_ORDER_ONLINE_PAYMENT;

                    }
                    break;
                default:
                    orderStatus = OrderStatus.DELIVERED_ORDER;

            }

            return orderStatus;

        }

        public static OrderStatusTracker getByActionName(String actionName) {
            return EnumUtils.getEnumList(OrderStatusTracker.class).stream()
                    .filter(item -> item.actionName.equalsIgnoreCase(actionName)).findFirst().orElse(NOT_FOUND_ACTION);
        }

        public static String getByActionNameAndServiceTypeCoce(String actionName, String classImplementTracker) {
            return EnumUtils.getEnumList(OrderStatusTracker.class).stream()
                    .filter(item -> item.actionName.equalsIgnoreCase(actionName))
                    .findFirst()
                    .map(res -> {

                        if (classImplementTracker.equalsIgnoreCase(TrackerImplementation.inkatracker.name())) {
                            return res.getTrackerStatus();
                        }

                        return res.getTrackerLiteStatus();


                    }).orElse(NOT_FOUND_ACTION.orderStatusError.getCode());
        }

        OrderStatusTracker(String trackerStatus, String trackerLiteStatus, OrderStatus orderStatus,
                           OrderStatus orderStatusError, String actionName) {
            this.trackerStatus = trackerStatus;
            this.trackerLiteStatus = trackerLiteStatus;
            this.orderStatus = orderStatus;
            this.orderStatusError = orderStatusError;
            this.actionName = actionName;
        }

        public String getTrackerStatus() {
            return trackerStatus;
        }

        public String getTrackerLiteStatus() {
            return trackerLiteStatus;
        }

        public OrderStatus getOrderStatus() {
            return orderStatus;
        }

        public OrderStatus getOrderStatusError() {
            return orderStatusError;
        }

        public String getActionName() {
            return actionName;
        }
    }

    enum OrderStatus {

        SUCCESS_FULFILLMENT_PROCESS("00", true),

        // ========== ERRORES =================================================================
        ERROR_INSERT_TRACKER("01", false), ERROR_INSERT_INKAVENTA("02", false),
        ERROR_RESERVED_ORDER("03", false), ERROR_PICKED("04", false),
        ERROR_PREPARED("05", false), ERROR_READY_FOR_PICKUP("05", false),
        ERROR_ASSIGNED("06", false), ERROR_ON_ROUTED("07", false),
        ERROR_ARRIVED("08", false), ERROR_DELIVERED("09", false),
        ERROR_CANCELLED("10", false),ERROR_REJECTED("10", false),
        // ==================================================================================================


        INVOICED("40", true), ERROR_INVOICED("41", false),

        SUCCESS_RESERVED_ORDER("10", true),

        CONFIRMED("15", true), CONFIRMED_TRACKER("16", true),

        ASSIGNED("17", true),

        PICKED_ORDER("18", true), PREPARED_ORDER("19", true),

        ON_ROUTED_ORDER("20",true), ARRIVED_ORDER("21",true),

        DELIVERED_ORDER("12", true), READY_PICKUP_ORDER("13", true),

        // se cancela una orden por que no hay stock
        CANCELLED_ORDER("11", true), CANCELLED_ORDER_NOT_ENOUGH_STOCK("31", true),
        CANCELLED_ORDER_ONLINE_PAYMENT("32", true),

        // se cancela una orden con pago en línea por falta de stock
        CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK("33", true),

        REJECTED_ORDER("34", true), // se cancela orden desde el pos unificado y/o otro cliente
        REJECTED_ORDER_ONLINE_PAYMENT("35", true),  ORDER_FAILED("38", false),

        // status cuando se llama al microservicio de visa - bbr
        LIQUIDATED_ONLINE_PAYMENT("42", true),
        SUCCESS_RESULT_ONLINE_PAYMENT("44", false),
        ERROR_RESULT_ONLINE_PAYMENT("-1", false),


        // =======================================================================================================

        NOT_FOUND_CODE("-1", false), NOT_FOUND_ORDER("-1", false), NOT_FOUND_ACTION("-1", false),
        EMPTY_RESULT_CANCELLATION("-1", false), EMPTY_RESULT_DISPATCHER("-1", false),
        EMPTY_RESULT_INKATRACKER("-1", false),

        EMPTY_RESULT_INKATRACKERLITE("-1", false), END_STATUS_RESULT("-1", false),
        EMPTY_RESULT_ORDERTRACKER("-1", false);


        private String code;
        private boolean isSuccess;

        OrderStatus(String code, boolean isSuccess) {
            this.code = code;
            this.isSuccess = isSuccess;

        }

        public static OrderStatus getByCode(String code) {
            return EnumUtils.getEnumList(OrderStatus.class).stream().filter(item -> item.code.equalsIgnoreCase(code))
                    .findFirst().orElse(NOT_FOUND_CODE);
        }

        public static OrderStatus getByName(String name) {
            return EnumUtils.getEnumList(OrderStatus.class).stream().filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst().orElse(NOT_FOUND_CODE);
        }

        public static boolean getFinalStatusByCode(String code) {
            return EnumUtils.getEnumList(OrderStatus.class).stream()
                    .anyMatch(item -> CANCELLED_ORDER_ONLINE_PAYMENT.code.equalsIgnoreCase(code)
                            || CANCELLED_ORDER.code.equalsIgnoreCase(code)
                            || DELIVERED_ORDER.code.equalsIgnoreCase(code));
        }

        public static boolean isToCreateOrderToOrderTracker(String code, String origin) {
            return EnumUtils
                    .getEnumList(OrderStatus.class)
                    .stream()
                    .anyMatch(item -> ORIGIN_BBR.equalsIgnoreCase(origin)
                            && (ERROR_INSERT_TRACKER.code.equalsIgnoreCase(code)
                                || ERROR_PICKED.code.equalsIgnoreCase(code)
                                || PICKED_ORDER.code.equalsIgnoreCase(code)
                                || ERROR_PREPARED.code.equalsIgnoreCase(code)
                                || CONFIRMED.code.equalsIgnoreCase(code)
                                || CONFIRMED_TRACKER.code.equalsIgnoreCase(code))
                    );
        }

        public String getCode() {
            return code;
        }

        public boolean isSuccess() {
            return isSuccess;
        }
    }

    enum Logical {

        Y(true, "1", 1), N(false, "0", 0);

        private final boolean value;
        private String valueString;
        private int valueInt;

        Logical(boolean value, String valueString, int valueInt) {

            this.value = value;
            this.valueString = valueString;
            this.valueInt = valueInt;
        }

        public String getValueString() {
            return valueString;
        }

        public boolean value() {
            return value;
        }

        public boolean isValue() {
            return value;
        }

        public int getValueInt() {
            return valueInt;
        }

        public static Logical parse(Boolean online) {
            if (Optional.ofNullable(online).orElse(false)) {
                return Y;
            }
            return N;
        }

        public static Logical getByValueString(String valueString) {
            return EnumUtils.getEnumList(Logical.class).stream().filter(
                    item -> Optional.ofNullable(valueString).orElse("0").equalsIgnoreCase(item.getValueString()))
                    .findFirst().orElse(N);
        }
    }

    String SUCCESS = "SUCCESS";
    String ERROR_PROCESS = "Functional service Error";
    Integer ONE_ATTEMPT = 1;
    String SUCCESS_CODE = "00";
    String NOT_DEFINED_CENTER = "NDC";
    String NOT_DEFINED_COMPANY = "NDC";
    String NOT_DEFINED_SERVICE = "NDS";
    String COMPANY_CODE_IFK = "IKF";
    String COMPANY_CODE_MF = "MF";
    Integer COLLECTION_PRESENTATION_ID = 3;
    String PICKUP = "PICKUP";
    String DELIVERY = "DELIVERY";
    String PICK_ORDER = "PICK_ORDER";
    String PREPARE_ORDER = "PREPARE_ORDER";
    String ASSIGN_ORDER = "ASSIGN_ORDER";
    String ON_ROUTE_ORDER = "ON_ROUTE_ORDER";
    String ARRIVAL_ORDER = "ARRIVAL_ORDER";
    String DELIVER_ORDER = "DELIVER_ORDER";
    String CANCEL_ORDER = "CANCEL_ORDER";
    String REJECT_ORDER = "REJECT_ORDER";
    String ORIGIN_OMNI_DELIVERY = "OMNI_DELIVERY";
    String ORIGIN_DIGITAL = "DIGITAL";
    String ORIGIN_BBR = "BBR";


    enum DeliveryManagerStatus {

        ORDER_FAILED("ERROR_INSERT_DM"), NONE("ERROR_NOT_IDENTIFIED");

        private String status;

        DeliveryManagerStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public static DeliveryManagerStatus getByName(String name) {

            return EnumUtils.getEnumList(DeliveryManagerStatus.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(NONE);
        }

    }

    interface SellerCenter {
        String BEAN_SERVICE_NAME = "sellerCenterService";

        enum ControversyTypes {
            CT("CT", "Controversia"),
            D("D", "Devolución"),
            DR("DR", "Solicitud de Devolución"),
            PP("PP", "No pagado"),
            RC("RC", "Despacho rechazado"),
            CC("CC", "Cancelado por el cliente"), 
            EC("EC", "Error de cliente"),
            PI("PI", "Primer despacho infructuoso"), 
            DI("DI", "Despacho infructuoso"),
            CS("CS", "Cancelado por el seller");

            private String type;
            private String description;

            ControversyTypes(String type, String description) {
                this.type = type;
                this.description = description;
            }

            public String getType() {
                return type;
            }

            public String getDescription() {
                return description;
            }
        }
    }

    interface ServiceTypeCodes {
        String PICKUP = "PICKUP";
    }
}
