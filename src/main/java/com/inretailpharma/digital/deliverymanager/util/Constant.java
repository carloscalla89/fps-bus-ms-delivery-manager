package com.inretailpharma.digital.deliverymanager.util;

import com.inretailpharma.digital.deliverymanager.proxy.InkatrackerLiteServiceImpl;
import com.inretailpharma.digital.deliverymanager.proxy.InkatrackerServiceImpl;
import com.inretailpharma.digital.deliverymanager.strategy.*;

import java.util.Optional;
import org.apache.commons.lang3.EnumUtils;

public interface Constant {

    enum ClassesImplements {

        OrderTrackerServiceImpl(TARGET_ORDER_TRACKER), InkatrackerLiteServiceImpl(TARGET_LITE),
        InkatrackerServiceImpl(TARGET_TRACKER), NONE(null);

        private String targetName;

        ClassesImplements(String targetName) {
            this.targetName = targetName;
        }

        public static ClassesImplements getByClass(Class<?> classType) {

            return EnumUtils.getEnumList(ClassesImplements.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(classType.getSimpleName())).findFirst().orElse(NONE);
        }

        public String getTargetName() {
            return targetName;
        }
    }

    enum CancellationStockDispatcher {

        CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK("C05", "Producto no disponible en el local"),
        CANCELLED_ORDER_NOT_ENOUGH_STOCK("C05", "Producto no disponible en el local"),
        CANCELLED_ORDER_BLACK_LIST("C46", "Cancelar orden lista negra"),
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

        public static String getDetailCancelStock(String name, String orderCancelDescription) {

            return EnumUtils.getEnumList(CancellationStockDispatcher.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().map(CancellationStockDispatcher::getReason)
                    .orElse(orderCancelDescription);
        }

        public static String getDetailCancelOrderForStock(String name, String orderCancelDescription, String detail) {

            return EnumUtils.getEnumList(CancellationStockDispatcher.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().map(res -> detail)
                    .orElse(orderCancelDescription);
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

    enum TrackerImplementation {
        inkatrackerlite(4, InkatrackerLiteServiceImpl.class, "DRUGSTORE", TARGET_LITE),
        inkatracker(3, InkatrackerServiceImpl.class,"DELIVERY_CENTER", TARGET_TRACKER),
        NONE(4, InkatrackerLiteServiceImpl.class, "DRUGSTORE", null);

        private Integer id;
        private Class trackerImplement;
        private String localType;
        private String targetName;

        TrackerImplementation(Integer id, Class trackerImplement, String localType, String targetName) {
            this.id = id;
            this.trackerImplement = trackerImplement;
            this.localType = localType;
            this.targetName = targetName;
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

        public String getTargetName() {
            return targetName;
        }

        public static TrackerImplementation getClassImplement(String classImplement) {

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
        String DEFAULT_INTERVAL_TIME_BY_SERVICE_ = "DEFAULT_INTERVAL_TIME_BY_SERVICE_";

        String ENABLED_SEND_TO_LIQUIDATION = "ENABLED_SEND_TO_LIQUIDATION";
    }

    interface InsinkErrorCode {
        String CODE_ERROR_STOCK = "E-1";
        String CODE_ERROR_BLACK_LIST = "E-2";
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
        String CANCELLED = "CANCELLED";
    }

    enum ActionOrder {

        ATTEMPT_TRACKER_CREATE(1, "reintento para enviar la orden a un tracker",1,
                METHOD_CREATE, RetryTracker.class),

        ATTEMPT_INSINK_CREATE(2, "reintento para enviar la órden al insink",1,
                METHOD_CREATE, RetryDeliveryDispatcher.class),

        REJECT_ORDER(4, "Acción para cambiar el estado de la orden como cancelada",9,
                METHOD_UPDATE, CancelOrder.class),

        CANCEL_ORDER(4, "Acción para cambiar el estado de la orden como cancelada",9,
                METHOD_UPDATE, CancelOrder.class),

        DELIVER_ORDER(4, "Acción para cambiar el estado de la orden como entregada",9,
                METHOD_UPDATE, DeliverOrder.class),

        READY_PICKUP_ORDER(4, "Acción para cambiar el estado de la orden como lista para recoger",5,
                METHOD_UPDATE, UpdateTracker.class),

        INVOICED_ORDER(4, "Acción para cambiar el estado de la orden a facturada",3,
                METHOD_UPDATE, UpdateTracker.class),

        // ========== nuevas actions que enviarán TI - 29-10-2020
        // =========================
        READY_FOR_BILLING(4, "Accion para cambiar el estado de la orden a READY_FOR_BILLING",5,
                METHOD_UPDATE, UpdateTracker.class),

        PICK_ORDER(4, "Acción para cambiar el estado de la orden a PICKEADO",4,
                METHOD_UPDATE, PickerOrder.class),

        READY_TO_ASSIGN(23, "Acción para cambiar el estado de la orden a 'Listo para asignar'", 4,
                METHOD_UPDATE, UpdateTracker.class),

        PREPARE_ORDER(4, "Acción para cambiar el estado de la orden a PREPADO", 5,
                METHOD_UPDATE, PrepareOrder.class),
        // =================================================================================

        ON_STORE_ORDER(4, "actualizar el BILLING ID(número de pedido diario) a un tracker",2,
                METHOD_UPDATE, UpdateTracker.class),

        // =========== nuevos actions que se enviarán desde el order-tracker
        ASSIGN_ORDER(4, "Acción para asignar órdenes",6,
                METHOD_UPDATE, UpdateTracker.class),

        UNASSIGN_ORDER(4, "Acción para asignar órdenes",6, METHOD_UPDATE, UpdateTracker.class),

        ON_ROUTE_ORDER(4, "Acción para CAMBIAR  al estado ON_ROUTE",7,
                METHOD_UPDATE, OnrouteOrder.class),

        ARRIVAL_ORDER(4, "Acción para asignar al estado ARRIVED",8,
                METHOD_UPDATE, UpdateTracker.class),

        SET_PARTIAL_ORDER(4, "Acción para actualizar una orden parcial",3,
                METHOD_UPDATE, UpdateTracker.class),

        CHECKOUT_ORDER(4, "Acción para poner el estado checkout al inkatracker",8,
                METHOD_UPDATE, UpdateTracker.class),

        FILL_ORDER(5, "Accion para llenar data del ecommerce a una orden",0,
                METHOD_CREATE, FillOrder.class),

        LIQUIDATED_ONLINE_PAYMENT(6, "Acción para informar la liquidacion del pago",10,
                METHOD_UPDATE, com.inretailpharma.digital.deliverymanager.strategy.OnlinePayment.class),

        LIQUIDATE_ORDER(7,"Acción para enviar el estado al dominio de liquidacion", 11,
                METHOD_UPDATE, LiquidationOrder.class),

        FILL_ORDER_CALL(8, "Accion para llenar data de call center ",0,
                METHOD_CREATE, FillOrderCall.class),

        BILL_ORDER(4, "Acción para informar el boleteo de una orden", 3,
                METHOD_UPDATE, BillOrder.class),

        NONE(0, "Not found status",0,METHOD_NONE, UpdateTracker.class);

        private Integer code;
        private String description;
        private int sequence;
        private String method;
        private Class<?> actionStrategyImplement;

        ActionOrder(Integer code, String description,int sequence, String method, Class actionStrategyImplement) {
            this.code = code;
            this.description = description;
            this.sequence = sequence;
            this.method = method;
            this.actionStrategyImplement = actionStrategyImplement;
        }



        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public int getSequence() {
            return sequence;
        }

        public String getMethod() {
            return method;
        }

        public static ActionOrder getByName(String name) {

            return EnumUtils.getEnumList(ActionOrder.class).stream().filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst().orElse(NONE);
        }

        public Class<?> getActionStrategyImplement() {
            return actionStrategyImplement;
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

        CANCELLED_ORDER_BLACK_LIST("CANCELLED", "CANCELLED", OrderStatus.CANCELLED_ORDER_BLACK_LIST, OrderStatus.ERROR_CANCELLED,
                ActionOrder.CANCEL_ORDER.name()),

        REJECTED_ORDER("REJECTED", "REJECTED", OrderStatus.REJECTED_ORDER, OrderStatus.ERROR_REJECTED,
                ActionOrder.REJECT_ORDER.name()),

        REJECTED_ORDER_ONLINE_PAYMENT("REJECTED", "REJECTED", OrderStatus.REJECTED_ORDER_ONLINE_PAYMENT,
                OrderStatus.ERROR_REJECTED, ActionOrder.REJECT_ORDER.name()),

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

        READY_TO_ASSIGN("READY_TO_ASSIGN", "READY_TO_ASSIGN",OrderStatus.READY_TO_ASSIGN,
                OrderStatus.ERROR_READY_TO_ASSIGN, ActionOrder.READY_TO_ASSIGN.name()),

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
                OrderStatus.ERROR_ARRIVED, ActionOrder.ARRIVAL_ORDER.name()),

        CHECKOUT_ORDER("CHECKOUT_ORDER", null, OrderStatus.CHECKOUT_ORDER,
                OrderStatus.ERROR_CHECKOUT, ActionOrder.CHECKOUT_ORDER.name());


        private String trackerStatus;
        private String trackerLiteStatus;
        private OrderStatus orderStatus;
        private OrderStatus orderStatusError;
        private String actionName;

        public static OrderStatusTracker getByName(String name) {
            return EnumUtils.getEnumList(OrderStatusTracker.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(NOT_FOUND_ACTION);
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

    enum LiquidationStatus {
        // Estados satisfactorioS al enviar desde el DM al componente de liquidación o a la auditoria
        PENDING("00",true,METHOD_CREATE), ERROR("02",true, METHOD_CREATE),
        AUTOMATIC_CANCELLED("03",true, METHOD_CREATE), IN_PROCESS("04",true, METHOD_UPDATE),
        BILLED("05", true, METHOD_UPDATE), PARTIAL_BILLED("06", true, METHOD_UPDATE),
        CANCELLED("07",true, METHOD_UPDATE), PENDING_LIQUIDATE("08",true, METHOD_UPDATE),

        // Estados de error al enviarse al componnte de liquidación
        ERROR_PENDING("60",false, METHOD_CREATE), ERROR_ERROR("61", false, METHOD_CREATE),
        ERROR_AUTOMATIC_CANCELLED("62", false, METHOD_CREATE), ERROR_IN_PROCESS("63", false, METHOD_UPDATE),
        ERROR_BILLED("64", false, METHOD_UPDATE), ERROR_PARTIAL_BILLED("65", false, METHOD_UPDATE),
        ERROR_CANCELLED("66",false, METHOD_UPDATE), ERROR_PENDING_LIQUIDATE("67",false, METHOD_UPDATE),
        NOT_FOUND_CODE("-1",false, METHOD_UPDATE),

        // Estados de error cuando falló algo al enviar al componente de liquidación
        ERROR_SENDING_CREATE_STATUS("68", false, METHOD_CREATE), ERROR_UPDATING_STATUS("69", false, METHOD_UPDATE);

        private String code;
        private boolean isSuccess;
        private String method;

        LiquidationStatus(String code, boolean isSuccess, String method) {
            this.code = code;
            this.isSuccess = isSuccess;
            this.method = method;
        }

        public String getCode() {
            return code;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public String getMethod() {
            return method;
        }

        public static LiquidationStatus getByStatusByName(String name) {
            return EnumUtils
                    .getEnumList(LiquidationStatus.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst().orElse(NOT_FOUND_CODE);
        }

        public static LiquidationStatus getErrorByStatusByName(String name) {
            return EnumUtils
                    .getEnumList(LiquidationStatus.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase("ERROR_"+name))
                    .findFirst().orElse(NOT_FOUND_CODE);
        }

        public static LiquidationStatus getStatusSuccessByErrorStatus(String name) {
            return EnumUtils
                    .getEnumList(LiquidationStatus.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name.substring("ERROR_".length())))
                    .findFirst().orElse(NOT_FOUND_CODE);
        }

    }


    enum OrderStatus {

        // ========== ERRORES =================================================================
        ERROR_INSERT_TRACKER("01", false), ERROR_INSERT_INKAVENTA("02", false),
        ERROR_PICKED("04", false),ERROR_READY_TO_ASSIGN("24",false),
        ERROR_PREPARED("05", false),
        ERROR_READY_FOR_PICKUP("05", false), ERROR_ASSIGNED("06", false),
        ERROR_ON_ROUTED("07", false), ERROR_ARRIVED("08", false),
        ERROR_DELIVERED("09", false), ERROR_CANCELLED("10", false),
        ERROR_REJECTED("10", false), ERROR_CHECKOUT("42",false),
        // ==================================================================================================


        INVOICED("40", true), ERROR_INVOICED("41", false),

        PARTIAL_UPDATE_ORDER("45", true), ERROR_PARTIAL_UPDATE("46", false),

        SUCCESS_RESERVED_ORDER("10", true),

        CONFIRMED("15", true), CONFIRMED_TRACKER("16", true),

        ASSIGNED("17", true),

        READY_TO_ASSIGN("23", true),

        PICKED_ORDER("18", true), PREPARED_ORDER("19", true),

        ON_ROUTED_ORDER("20",true), ARRIVED_ORDER("21",true),

        CHECKOUT_ORDER("22",true),

        DELIVERED_ORDER("12", true), READY_PICKUP_ORDER("13", true),

        // se cancela una orden por que no hay stock
        CANCELLED_ORDER("11", true), CANCELLED_ORDER_NOT_ENOUGH_STOCK("31", true),
        CANCELLED_ORDER_ONLINE_PAYMENT("32", true),

        // se cancela una orden con pago en línea por falta de stock
        CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK("33", true),

        REJECTED_ORDER("34", true), // se cancela orden desde el pos unificado y/o otro cliente
        REJECTED_ORDER_ONLINE_PAYMENT("35", true),  ORDER_FAILED("38", false),

        // status cuando se llama al microservicio de visa - bbr
        LIQUIDATED_ONLINE_PAYMENT("43", true),

        CANCEL_ORDER("47", true),
        SUCCESS_RESULT_ONLINE_PAYMENT("44", false),
        ERROR_RESULT_ONLINE_PAYMENT("-1", false),

        NOT_FOUND_CODE("-1", false), NOT_FOUND_ORDER("-1", false), NOT_FOUND_ACTION("-1", false),
        EMPTY_RESULT_CANCELLATION("-1", false), EMPTY_RESULT_DISPATCHER("-1", false),
        EMPTY_RESULT_INKATRACKER("-1", false),

        EMPTY_RESULT_INKATRACKERLITE("-1", false), END_STATUS_RESULT("-1", false),
        EMPTY_RESULT_ORDERTRACKER("-1", false),

        BILLED_ORDER("47", true),
        CANCELLED_ORDER_BLACK_LIST("48", true);

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
                            || CANCELLED_ORDER_ONLINE_PAYMENT_NOT_ENOUGH_STOCK.code.equalsIgnoreCase(code)
                            || CANCELLED_ORDER_NOT_ENOUGH_STOCK.code.equalsIgnoreCase(code)
                            || DELIVERED_ORDER.code.equalsIgnoreCase(code)
                            || REJECTED_ORDER_ONLINE_PAYMENT.code.equalsIgnoreCase(code)
                            || REJECTED_ORDER.code.equalsIgnoreCase(code));
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
    String CHECKOUT_ORDER = "CHECKOUT_ORDER";
    String INVOICED_ORDER = "INVOICED_ORDER";
    String PICK_ORDER = "PICK_ORDER";
    String READY_TO_ASSIGN = "READY_TO_ASSIGN";
    String PREPARE_ORDER = "PREPARE_ORDER";
    String ASSIGN_ORDER = "ASSIGN_ORDER";
    String ON_ROUTE_ORDER = "ON_ROUTE_ORDER";
    String ARRIVAL_ORDER = "ARRIVAL_ORDER";
    String DELIVER_ORDER = "DELIVER_ORDER";
    String CANCEL_ORDER = "CANCEL_ORDER";
    String REJECT_ORDER = "REJECT_ORDER";
    String ORIGIN_OMNI_DELIVERY = "OMNI_DELIVERY";
    String ORIGIN_DRUGSTORE_ENGINE = "DRUGSTORE_ENGINE";
    String ORIGIN_DIGITAL = "DIGITAL";
    String ORIGIN_FARMADASHBOARD = "FARMADASHBOARD";
    String ORIGIN_INKATRACKER_WEB = "INKATRACKER_WEB";
    String ORIGIN_DCPROXY = "INKAPROXY";
    String ORIGIN_TRACKER= "TRACKER";
    String ORIGIN_BBR = "BBR";
    String ORIGIN_UNIFIED_POS = "UNIFIED_POS";
    String ORIGIN_TASK = "TASK";
    String ORIGIN_TASK_EXPIRATION = "TASK_EXPIRATION";
    String ORIGIN_DELIVERY_MANAGER = "DELIVERY_MANAGER";
    String TARGET_TRACKER = "TRACKER";
    String TARGET_LITE = "LITE";
    String TARGET_ORDER_TRACKER = "ORDER_TRACKER";
    String TARGET_LIQUIDATION = "LIQUIDATION";
    String TARGET_INSINK = "INSINK";
    String TARGET_SELLER = "SELLER";
    String UPDATED_BY_INIT = "INIT";
    String UPDATED_BY_INKATRACKER_WEB = "INKATRACKER_WEB";
    String METHOD_UPDATE = "UPDATE";
    String METHOD_CREATE = "CREATE";
    String METHOD_NONE = "NONE";
    String TASK_LAMBDA_UPDATED_BY = "LAMBDA";
    String SOURCE_AGORA = "AGORA";
    String SOURCE_RAPPI = "RAPPI";
    String SOURCE_SELLER_CENTER = "SC";
    String[] ORDER_STATUS_RAPPI = {"CONFIRMED_TRACKER", "READY_PICKUP_ORDER", "PICK_ORDER", "CANCEL_ORDER"};
    String DU_CANCEL_CODE = "DU";

    String ACTION_DELIVER_ORDER = "DELIVER_ORDER";
    String ACTION_CANCEL_ORDER = "CANCEL_ORDER";
    String ACTION_REJECT_ORDER = "REJECT_ORDER";

    String LIQUIDATION_STATUS_CANCELLED_CODE = "07";
    String LIQUIDATION_STATUS_CANCELLED = "CANCELLED";
    String LIQUIDATION_STATUS_PENDING_CODE = "08";
    String LIQUIDATION_STATUS_PENDING = "PENDING_LIQUIDATE";
    String LIQUIDATION_STATUS_BILLED_CODE = "05";
    String LIQUIDATION_STATUS_BILLED = "BILLED";

    double VALUE_ZERO_DOUBLE = 0.0;
    String VALUE_ZERO_STRING = "0";
    
    String DEFAULT_SALE_CHANNEL_TYPE = "Digital";

    enum DeliveryManagerStatus {

        ORDER_FAILED("-01","ERROR_INSERT_DM"), NONE("-02","ERROR_NOT_IDENTIFIED");
        private String code;
        private String status;

        DeliveryManagerStatus(String code, String status) {
            this.code = code;
            this.status = status;
        }

        public String getCode() {
            return code;
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
            CS("CS", "Cancelado por el seller"),
        	CP("CP", "Cerrado - procede");
        	
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

    enum StockType {
        B("BACKUP"), M("MAIN");

        private String description;

        public String getDescription() {
            return description;
        }

        StockType(String description) {
            this.description = description;
        }

        public static StockType getByCode(String name) {

            return EnumUtils.getEnumList(StockType.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(M);
        }

    }

    interface OnlineLiquidation {
        String LIQUIDATE = "LIQUIDATE_ORDER";
        String REFUND = "REFUND_ORDER";
    }

    enum DeliveryType {
        PROG("Programado"),
        AM_PM("AM/PM"),
        EXP("Express"),
        RET("Retiro en tienda");

        private String description;

        public String getDescription() {
            return description;
        }

        DeliveryType(String description) {
            this.description = description;
        }

        public static DeliveryType getByName(String name) {

            return EnumUtils.getEnumList(DeliveryType.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(PROG);
        }

    }

    interface TimeLimitFilterDate {
        int TIME_LIMIT_GRID = 1;
        int TIME_LIMIT_OTHER = 3;
        int TIME_PLUS_FUTURE_DAYS = 10;
    }

    interface FilterOption {
        String FIND_ORDER_NUMBER = "1";
        String FIND_TELEPHONE_NUMBER = "2";
        String FIND_DOCUMENT_NUMBER = "3";
    }

    interface OrderCriteriaColumn {
        String ORDER_CRITERIA_ECOMMERCE_ID = "1";
        String ORDER_CRITERIA_STORE = "2";
        String ORDER_CRITERIA_CHANNEL = "3";
        String ORDER_CRITERIA_SERVICE_TYPE = "4";
        String ORDER_CRITERIA_DATE = "5";
        String ORDER_CRITERIA_CLIENT = "6";
        String ORDER_CRITERIA_DOCUMENT = "7";
        String ORDER_CRITERIA_STATUS = "8";
    }

    enum OrderCriteria {
        A("ASC"), D("DESC");

        private String order;

        public String getOrder() {
            return order;
        }

        OrderCriteria(String order) {
            this.order = order;
        }

        public static OrderCriteria getByCode(String name) {

            return EnumUtils.getEnumList(OrderCriteria.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(D);
        }

    }

    enum PaymentType {
        CARD("Pago con POS"),
        CASH("Pago efectivo"),
        CASH_DOLAR("Pago efectivo"),
        ONLINE_PAYMENT("Pago en linea");

        private String paymentTypeDescription;

        public String getPaymentTypeDescription() {
            return paymentTypeDescription;
        }

        PaymentType(String paymentTypeDescription) {
            this.paymentTypeDescription = paymentTypeDescription;
        }

        public static PaymentType getByCode(String name) {

            return EnumUtils.getEnumList(PaymentType.class).stream()
                    .filter(item -> item.name().equalsIgnoreCase(name)).findFirst().orElse(CASH);
        }

    }
}