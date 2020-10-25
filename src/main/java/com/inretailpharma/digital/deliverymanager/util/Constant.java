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
        INKATRACKER_LITE_RAD(4,"inkatrackerlite","RAD"), INKATRACKER_LITE_RET(4,"inkatrackerlite","RET"),
        INKATRACKER_LITE_EXP(4,"inkatrackerlite","EXP"), INKATRACKER_LITE_PROG(4,"inkatrackerlite","PROG"),
        INKATRACKER_LITE_AM_PM(4,"inkatrackerlite","AM_PM"), INKATRACKER_LITE_CALL_RAD(4,"inkatrackerlite","RAD"),
        INKATRACKER_LITE_CALL_EXP(4,"inkatrackerlite","EXP"), INKATRACKER_LITE_CALL_PROG(4,"inkatrackerlite","PROG"),
        INKATRACKER_LITE_CALL_AM_PM(4,"inkatrackerlite","AM_PM"),

        INKATRACKER_RAD(3,"inkatracker","RAD"), INKATRACKER_EXP(3,"inkatracker","EXP"),
        INKATRACKER_PROG(3,"inkatracker","PROG"), INKATRACKER_AM_PM(3,"inkatracker","AM_PM"),

        TEMPORARY_RAD(2,"temporary","RAD"), TEMPORARY_EXP(2,"temporary","EXP"),
        TEMPORARY_PROG(2,"temporary","PROG"), TEMPORARY_AM_PM(2,"temporary","AM_PM"),

        NONE(3,"not_found","RAD");

        private int id;
        private String name;
        private String serviceTypeCode;

        TrackerImplementation(int id, String name, String serviceTypeCode) {
            this.id = id;
            this.name = name;
            this.serviceTypeCode = serviceTypeCode;
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

        String ACTIVATED_ORDER_TRACKER_VALUE = "1";
        String ACTIVATED_ORDER_TRACKER = "ACTIVATED_ORDER_TRACKER";

        String DAYS_PICKUP_MAX_RET = "DAYS_PICKUP_MAX_RET";
        String ACTIVATED_DD_IKF = "ACTIVATED_DD_IKF";
        String ACTIVATED_DD_MF = "ACTIVATED_DD_MF";
    }

    interface InsinkErrorCode {
        String CODE_ERROR_STOCK = "E-1";
    }

    interface ActionName {
        String RELEASE_ORDER = "RELEASE_ORDER";
        String CANCEL_ORDER = "CANCEL_ORDER";
        String DELIVER_ORDER = "DELIVER_ORDER";
        String READY_PICKUP_ORDER = "READY_PICKUP_ORDER";

        String ATTEMPT_TRACKER_CREATE = "ATTEMPT_TRACKER_CREATE";
        String UPDATE_TRACKER_BILLING = "UPDATE_TRACKER_BILLING";
        String UPDATE_RELEASE_ORDER = "UPDATE_RELEASE_ORDER";
        String INVOICED_ORDER = "INVOICED_ORDER";
    }

    interface ActionNameInkatrackerlite {
        String READY_FOR_PICKUP = "READY_FOR_PICKUP";
        String CANCELLED = "CANCELLED";
        String DELIVERED = "DELIVERED";
        String READY_FOR_BILLING = "READY_FOR_BILLING";
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
        DESCHEDULER_ORDER(5, "Desprogramar una orden en el inkatracker"),
        ATTEMPT_INSINK_CREATE(2, "reintento para enviar la órden al insink"),
        RELEASE_ORDER(2, "Liberar orden reservada"),

        UPDATE_RELEASE_ORDER(3, "Actualizar el resultado al liberar unaorden desde el dispatcher"),

        CANCEL_ORDER(4, "Acción para cambiar el estado de la orden como cancelada"),
        DELIVER_ORDER(4, "Acción para cambiar el estado de la orden como entregada"),
        READY_PICKUP_ORDER(4, "Acción para cambiar el estado de la orden como lista para recoger"),
        INVOICED_ORDER(4, "Acción para cambiar el estado de la orden a facturada"),

        ON_STORE_ORDER(2, "Acción para actualizar el estado en tienda"),

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
        CANCEL_ORDER("11","CANCELLED", OrderStatus.CONFIRMED_CANCEL_TRACKER),
        ERROR_TO_CANCEL_ORDER("33","CANCELLED",OrderStatus.ERROR_TO_CANCEL_ORDER),
        ERROR_INSERT_TRACKER("01","CONFIRMED",OrderStatus.ERROR_INSERT_TRACKER),
        CANCELLED_ORDER("11","CANCELLED", OrderStatus.CONFIRMED_CANCEL_TRACKER),
        CANCELLED_ORDER_ONLINE_PAYMENT("37","CANCELLED",OrderStatus.CONFIRMED_CANCEL_TRACKER),
        DELIVER_ORDER("12","DELIVERED",OrderStatus.CONFIRMED_DELIVERY_TRACKER),
        DELIVERED_ORDER("12","DELIVERED",OrderStatus.CONFIRMED_DELIVERY_TRACKER),
        ATTEMPT_TRACKER_CREATE("15","CONFIRMED",OrderStatus.CONFIRMED_TRACKER),
        CONFIRMED("15","CONFIRMED",OrderStatus.CONFIRMED_TRACKER),
        SUCCESS_RESERVED_ORDER("10","CONFIRMED",OrderStatus.CONFIRMED_TRACKER),
        NOT_FOUND_ACTION("-1","NOT_FOUND_ACTION",OrderStatus.CONFIRMED_TRACKER);


        private String code;
        private String status;
        private OrderStatus orderStatus;

        public static OrderStatusTracker getByName(String name) {
            return EnumUtils.getEnumList(OrderStatusTracker.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(NOT_FOUND_ACTION);
        }

        OrderStatusTracker(String code, String status, OrderStatus orderStatus) {
            this.code = code;
            this.status = status;
            this.orderStatus = orderStatus;
        }

        public String getCode() {
            return code;
        }

        public String getStatus() {
            return status;
        }

        public OrderStatus getOrderStatus() {
            return orderStatus;
        }
    }

    enum OrderStatus {

        SUCCESS_FULFILLMENT_PROCESS("00", true),

        ERROR_INSERT_TRACKER("01", false),
        ERROR_INSERT_INKAVENTA("02", false),
        ERROR_RESERVED_ORDER("03",  false),
        ERROR_RELEASE_ORDER("04",  false),
        ERROR_RELEASE_DISPATCHER_ORDER("04",  false),
        ERROR_UPDATE_TRACKER_BILLING("05", false),
        ERROR_ON_STORE("06",  false),
        ERROR_ASSIGNED("07",  false),
        ERROR_PICKED("08",  false),
        ERROR_PREPARED("09",  false),

        ERROR_CONFIRMED("30",  false),
        ERROR_ARRIVE("31",  false),
        ERROR_REJECT("32",  false),
        ERROR_TO_CANCEL_ORDER("33",  false),
        ERROR_DELIVER("34",  false),
        ERROR_PICKUP("35",  false),
        ERROR_UPDATE("36",  false),
        CANCELLED_ORDER_ONLINE_PAYMENT("37",  true),
        ORDER_FAILED("38",  false),
        INVOICED("40", false),
        ERROR_INVOICED("41", false),

        SUCCESS_RESERVED_ORDER("10", true),

        CANCELLED_ORDER("11",true),
        DELIVERED_ORDER("12",  false),
        READY_PICKUP_ORDER("13",  true),
        RELEASED_ORDER("14", true),

        CONFIRMED("15",  true),

        CONFIRMED_TRACKER("16",  true),
        CONFIRMED_CANCEL_TRACKER("17",  true),
        CONFIRMED_DELIVERY_TRACKER("18",  true),

        ON_STORE("16",  true),
        ASSIGNED("17",  true),
        PICKED_ORDER("18",  true),
        PREPARED_ORDER("19",  true),
        ARRIVED("20",  true),
        REJECTED("21",  true),

        NOT_FOUND_CODE("-1",  false),
        NOT_FOUND_ORDER("-1",  false),
        NOT_DEFINED_ERROR("-1",  false),
        NOT_DEFINED_STATUS("-1",  false),
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
