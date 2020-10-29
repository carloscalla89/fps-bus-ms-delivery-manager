package com.inretailpharma.digital.deliverymanager.util;

import org.apache.commons.lang3.EnumUtils;

import java.util.Optional;

public interface Constant {

    enum StatusDispatcherResult {

        INVALID_STRUCTURE("ERROR_INSERT_INKAVENTA"),
        ORDER_RESERVED("SUCCESS_RESERVED_ORDER"),
        ORDER_REGISTERED("CONFIRMED"),
        NOT_ENOUGH_STOCK("CANCELLED_ORDER"),
        NOT_ENOUGH_STOCK_PAYMENT_ONLINE("CANCELLED_ORDER_ONLINE_PAYMENT"),
        ORDER_FAILED("ERROR_INSERT_INKAVENTA"),
        NONE("ERROR_INSERT_INKAVENTA");

        private String status;

        StatusDispatcherResult(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public static StatusDispatcherResult getByName(String name) {

            return EnumUtils.getEnumList(StatusDispatcherResult.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(NONE);
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

            return EnumUtils.getEnumList(DispatcherImplementation.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(companyCode))
                    .findFirst()
                    .orElse(IKF);
        }

    }


    enum TrackerImplementation {
        INKATRACKER_LITE_RAD(4,"inkatrackerlite","RAD", "RAD"),
        INKATRACKER_LITE_EXP(4,"inkatrackerlite","EXP", "RAD"),
        INKATRACKER_LITE_PROG(4,"inkatrackerlite","PROG","RAD"),
        INKATRACKER_LITE_AM_PM(4,"inkatrackerlite","AM_PM", "RAD"),
        INKATRACKER_LITE_RET(4,"inkatrackerlite","RET","RET"),
        INKATRACKER_LITE_CALL_RAD(4,"inkatrackerlite","RAD", "RAD"),
        INKATRACKER_LITE_CALL_EXP(4,"inkatrackerlite","EXP", "RAD"),
        INKATRACKER_LITE_CALL_PROG(4,"inkatrackerlite","PROG","RAD"),
        INKATRACKER_LITE_CALL_AM_PM(4,"inkatrackerlite","AM_PM", "RAD"),
        INKATRACKER_LITE_CALL_RET(4,"inkatrackerlite","RET", "RET"),

        INKATRACKER_RAD(3,"inkatracker","RAD", "RAD"),
        INKATRACKER_EXP(3,"inkatracker","EXP", "RAD"),
        INKATRACKER_PROG(3,"inkatracker","PROG", "RAD"),
        INKATRACKER_AM_PM(3,"inkatracker","AM_PM", "RAD"),

        TEMPORARY_RAD(2,"temporary","RAD", "RAD"),
        TEMPORARY_EXP(2,"temporary","EXP", "RAD"),
        TEMPORARY_PROG(2,"temporary","PROG", "RAD"),
        TEMPORARY_AM_PM(2,"temporary","AM_PM", "RAD"),
        TEMPORARY_RET(2,"temporary","RET", "RET"),

        NONE(3,"not_found","RAD", "RAD");

        private int id;
        private String name;
        private String serviceTypeCode;
        private String serviceTypeCodeOld;

        TrackerImplementation(int id, String name, String serviceTypeCode,String serviceTypeCodeOld) {
            this.id = id;
            this.name = name;
            this.serviceTypeCode = serviceTypeCode;
            this.serviceTypeCodeOld = serviceTypeCodeOld;
        }


        public static TrackerImplementation getByCode(String code) {

            return EnumUtils.getEnumList(TrackerImplementation.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(code))
                    .findFirst()
                    .orElse(NONE);
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getServiceTypeCode() {
            return serviceTypeCode;
        }

        public String getServiceTypeCodeOld() {
            return serviceTypeCodeOld;
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

    enum ActionOrder {

        ATTEMPT_TRACKER_CREATE(1, "reintento para enviar la orden a un tracker"),
        UPDATE_TRACKER_BILLING(1, "actualizar el BILLING ID(número de pedido diario) a un tracker"),
        ON_RELEASE_ORDER(1, "actualizar el BILLING ID(número de pedido diario) a un tracker"),
        ON_STORE_ORDER(1, "actualizar el BILLING ID(número de pedido diario) a un tracker"),


        ATTEMPT_INSINK_CREATE(2, "reintento para enviar la órden al insink"),

        RELEASE_ORDER(2, "Liberar orden reservada"),

        UPDATE_RELEASE_ORDER(3, "Actualizar el resultado al liberar unaorden desde el dispatcher"),

        CANCEL_ORDER(4, "Acción para cambiar el estado de la orden como cancelada"),
        DELIVER_ORDER(4, "Acción para cambiar el estado de la orden como entregada"),
        READY_PICKUP_ORDER(4, "Acción para cambiar el estado de la orden como lista para recoger"),
        INVOICED_ORDER(4, "Acción para cambiar el estado de la orden a facturada"),

        //========== nuevas actions que enviarán TI - 29-10-2020 =========================
        READY_FOR_BILLING(4,"Accion para cambiar el estado de la orden a READY_FOR_BILLING"),
        PICK_ORDER(4, "Acción para cambiar el estado de la orden a PICKEADO"),
        PREPARE_ORDER(4, "Acción para cambiar el estado de la orden a PREPADO"),
        //=================================================================================


        FILL_ORDER(5,"Accion para llenar data del ecommerce a una orden"),

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

            return EnumUtils.getEnumList(ActionOrder.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(NONE);
        }
    }

    enum OrderStatusTracker {

        UPDATE_TRACKER_BILLING("UPDATE_TRACKER_BILLING", "UPDATE_TRACKER_BILLING", OrderStatus.CONFIRMED_TRACKER,
                OrderStatus.ERROR_INSERT_TRACKER, ActionOrder.UPDATE_TRACKER_BILLING.name()),

        RELEASE_ORDER("ON_STORE_ORDER", "ON_STORE", OrderStatus.CONFIRMED_TRACKER,
                OrderStatus.ERROR_INSERT_TRACKER, ActionOrder.RELEASE_ORDER.name()),

        ON_RELEASE_ORDER("ON_STORE_ORDER", "ON_STORE", OrderStatus.CONFIRMED_TRACKER,
                OrderStatus.ERROR_INSERT_TRACKER, ActionOrder.ON_RELEASE_ORDER.name()),

        ON_STORE_ORDER("ON_STORE_ORDER", "ON_STORE", OrderStatus.CONFIRMED_TRACKER,
                OrderStatus.ERROR_INSERT_TRACKER, ActionOrder.ON_STORE_ORDER.name()),

        CANCELLED_ORDER("CANCELLED", "CANCELLED", OrderStatus.CANCELLED_ORDER,
                OrderStatus.ERROR_TO_CANCEL_ORDER, ActionOrder.CANCEL_ORDER.name()),

        CANCELLED_ORDER_ONLINE_PAYMENT("CANCELLED", "CANCELLED",OrderStatus.CANCELLED_ORDER_ONLINE_PAYMENT,
                OrderStatus.ERROR_TO_CANCEL_ORDER, ActionOrder.NONE.name()),

        ERROR_INSERT_TRACKER("CONFIRMED", "CONFIRMED",OrderStatus.ERROR_INSERT_TRACKER,
                OrderStatus.ERROR_INSERT_TRACKER, ActionOrder.ATTEMPT_TRACKER_CREATE.name()),

        ERROR_RESERVED_ORDER("CONFIRMED", "CONFIRMED",OrderStatus.ERROR_INSERT_TRACKER,
                OrderStatus.ERROR_INSERT_INKAVENTA, ActionOrder.NONE.name()),

        CONFIRMED("CONFIRMED", "CONFIRMED",OrderStatus.CONFIRMED,
                OrderStatus.ERROR_INSERT_INKAVENTA, ActionOrder.NONE.name()),

        CONFIRMED_TRACKER("CONFIRMED", "CONFIRMED",OrderStatus.CONFIRMED_TRACKER,
                OrderStatus.ERROR_INSERT_INKAVENTA, ActionOrder.NONE.name()),

        SUCCESS_RESERVED_ORDER("CONFIRMED", "CONFIRMED",OrderStatus.CONFIRMED_TRACKER,
                OrderStatus.ERROR_INSERT_INKAVENTA, ActionOrder.NONE.name()),

        DELIVERED_ORDER("DELIVER_ORDER", "DELIVERED", OrderStatus.DELIVERED_ORDER,
                OrderStatus.ERROR_DELIVER, ActionOrder.DELIVER_ORDER.name()),

        NOT_FOUND_ACTION("NOT_FOUND_ACTION", "NOT_FOUND_ACTION",OrderStatus.NOT_FOUND_CODE,
                OrderStatus.NOT_FOUND_CODE, ActionOrder.NONE.name()),

        INVOICED_ORDER("INVOICED_ORDER", "INVOICED",OrderStatus.INVOICED,
                OrderStatus.ERROR_INVOICED, ActionOrder.INVOICED_ORDER.name()),


        PICKED_ORDER("PICKING_ORDER", "PICKING",OrderStatus.PICKED_ORDER,
                OrderStatus.ERROR_PICKED, ActionOrder.PICK_ORDER.name()),

        PREPARED_ORDER("PREPARED_ORDER", "PREPARED",OrderStatus.PREPARED_ORDER,
                OrderStatus.ERROR_PREPARED, ActionOrder.PREPARE_ORDER.name()),


        READY_FOR_BILLING("READY_FOR_BILLING", "READY_FOR_BILLING",OrderStatus.READY_PICKUP_ORDER,
                           OrderStatus.ERROR_READY_FOR_PICKUP, ActionOrder.READY_FOR_BILLING.name()),

        READY_PICKUP_ORDER("READY_FOR_PICKUP", "READY_FOR_PICKUP",OrderStatus.READY_PICKUP_ORDER,
                OrderStatus.ERROR_READY_FOR_PICKUP, ActionOrder.READY_PICKUP_ORDER.name());


        private String trackerStatus;
        private String trackerLiteStatus;
        private OrderStatus orderStatus;
        private OrderStatus orderStatusError;
        private String actionName;

        public static OrderStatusTracker getByName(String name) {
            return EnumUtils.getEnumList(OrderStatusTracker.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(NOT_FOUND_ACTION);
        }

        public static OrderStatusTracker getByActionName(String actionName) {
            return EnumUtils.getEnumList(OrderStatusTracker.class)
                    .stream()
                    .filter(item -> item.actionName.equalsIgnoreCase(actionName))
                    .findFirst()
                    .orElse(NOT_FOUND_ACTION);
        }

        OrderStatusTracker(String trackerStatus, String trackerLiteStatus, OrderStatus orderStatus, OrderStatus orderStatusError, String actionName) {
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

        ERROR_INSERT_TRACKER("01", false),
        ERROR_INSERT_INKAVENTA("02", false),
        ERROR_RESERVED_ORDER("03",  false),
        ERROR_RELEASE_DISPATCHER_ORDER("04",  false),
        ERROR_UPDATE_TRACKER_BILLING("05", false),
        ERROR_READY_FOR_PICKUP("06",  false),
        ERROR_ASSIGNED("07",  false),
        ERROR_PICKED("08",  false),
        ERROR_PREPARED("09",  false),

        ERROR_TO_CANCEL_ORDER("33",  false),
        ERROR_DELIVER("34",  false),
        CANCELLED_ORDER_ONLINE_PAYMENT("37",  true),
        ORDER_FAILED("38",  false),
        INVOICED("40", true),
        ERROR_INVOICED("41", false),

        SUCCESS_RESERVED_ORDER("10", true),

        CANCELLED_ORDER("11",true),
        DELIVERED_ORDER("12",  true),
        READY_PICKUP_ORDER("13",  true),

        CONFIRMED("15",  true),
        CONFIRMED_TRACKER("16",  true),


        ASSIGNED("17",  true),

        PREPARED_ORDER("18",  true),
        PICKED_ORDER("19",  true),

        NOT_FOUND_CODE("-1",  false),
        NOT_FOUND_ORDER("-1",  false),
        NOT_FOUND_ACTION("-1",  false),
        EMPTY_RESULT_CANCELLATION("-1", false),
        EMPTY_RESULT_DISPATCHER("-1",  false),
        EMPTY_RESULT_INKATRACKER("-1", false),

        EMPTY_RESULT_TEMPORARY("-1", false),
        EMPTY_RESULT_INKATRACKERLITE("-1", false),
        END_STATUS_RESULT("-1",  false);


        private String code;
        private boolean isSuccess;

        OrderStatus(String code, boolean isSuccess) {
            this.code = code;
            this.isSuccess = isSuccess;

        }

        public static OrderStatus getByCode(String code) {
            return EnumUtils.getEnumList(OrderStatus.class)
                    .stream()
                    .filter(item -> item.code.equalsIgnoreCase(code))
                    .findFirst()
                    .orElse(NOT_FOUND_CODE);
        }

        public static OrderStatus getByName(String name) {
            return EnumUtils.getEnumList(OrderStatus.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(NOT_FOUND_CODE);
        }

        public static boolean getFinalStatusByCode(String code) {
            return EnumUtils.getEnumList(OrderStatus.class)
                    .stream()
                    .anyMatch(item -> CANCELLED_ORDER_ONLINE_PAYMENT.code.equalsIgnoreCase(code)
                            || CANCELLED_ORDER.code.equalsIgnoreCase(code)
                            || DELIVERED_ORDER.code.equalsIgnoreCase(code)
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

        Y(true, "1", 1), N(false, "0",0);

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
            return EnumUtils.getEnumList(Logical.class)
                    .stream()
                    .filter(item -> Optional.ofNullable(valueString).orElse("0").equalsIgnoreCase(item.getValueString()))
                    .findFirst()
                    .orElse(N);
        }
    }

    interface Constans {
        Integer ONE_ATTEMPT = 1;
        String SUCCESS_CODE = "00";
        String NOT_DEFINED_CENTER = "NDC";
        String NOT_DEFINED_COMPANY = "NDC";
        String NOT_DEFINED_SERVICE = "NDS";
        String COMPANY_CODE_IFK = "IKF";
        String COMPANY_CODE_MF = "MF";
    }

    enum DeliveryManagerStatus {

        ORDER_FAILED("ERROR_INSERT_DM"),
        NONE("ERROR_NOT_IDENTIFIED");

        private String status;

        DeliveryManagerStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }

        public static DeliveryManagerStatus getByName(String name) {

            return EnumUtils.getEnumList(DeliveryManagerStatus.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(NONE);
        }

    }
}
