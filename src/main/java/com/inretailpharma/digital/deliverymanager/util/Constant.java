package com.inretailpharma.digital.deliverymanager.util;

import org.apache.commons.lang3.EnumUtils;

import java.util.Optional;

public interface Constant {

    enum TrackerImplementation {
        INKATRACKER_LITE_RAD(4,"inkatrackerlite"), INKATRACKER_LITE_RET(4,"inkatrackerlite"),
        INKATRACKER_RAD(3,"inkatracker"), TEMPORARY_RAD(2,"temporary"), NONE(3,"not_found");

        private int id;
        private String name;

        TrackerImplementation(int id, String name) {
            this.id = id;
            this.name = name;
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
    }

    interface InsinkErrorCode {
        String CODE_ERROR_GENERAL = "E-0";
        String CODE_ERROR_CLIENT_CONNECTION = "C-0";
        String CODE_ERROR_STOCK = "E-1";
    }

    interface ActionName {
        String RELEASE_ORDER = "RELEASE_ORDER";
        String CANCEL_ORDER = "CANCEL_ORDER";
        String DELIVER_ORDER = "DELIVER_ORDER";
        String READY_PICKUP_ORDER = "READY_PICKUP_ORDER";
    }

    interface ActionNameInkatrackerlite {
        String READY_FOR_PICKUP = "READY_FOR_PICKUP";
        String CANCELLED = "CANCELLED";
        String DELIVERED = "DELIVERED";
        String READY_FOR_BILLING = "READY_FOR_BILLING";
    }

    enum Source {
        SC;
    }
    enum ReceiptType {
        TICKET("BOLETA"),
        INVOICE("FACTURA"),
        UNDEFINED("NO DEFINIDO");

        private final String description;

        ReceiptType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static ReceiptType getByName(String name) {
            if (TICKET.name().equalsIgnoreCase(name))
                return TICKET;
            if (INVOICE.name().equalsIgnoreCase(name))
                return INVOICE;
            return UNDEFINED;
        }
    }
    long DS_INKATRACKER = 3L;
    long DEFAULT_DRUGSTORE_ID = 36;
    int MAX_DELIVERY_NOTES_LENGTH = 200;
    String NOTE_SEPARATOR = " - ";
    String DEFAULT_DS = "RAD";
    int DEFAULT_SC_CARD_PROVIDER_ID = 1;
    int DEFAULT_SC_PAYMENT_METHOD_ID = 3;
    String DEFAULT_SC_PAYMENT_METHOD_VALUE = "Pago en línea";
    String RECEIVER_FORMAT = "Recibe: %s";

    enum ActionOrder {

        ATTEMPT_TRACKER_CREATE(1, "reintento para enviar la orden a un tracker", null, null),
        UPDATE_TRACKER_BILLING(1, "actualizar el BILLING ID(número de pedido diario) a un tracker", null, null),

        ATTEMPT_INSINK_CREATE(2, "reintento para enviar la órden al insink", null, null),
        RELEASE_ORDER(2, "Liberar orden reservada", null, null),

        UPDATE_RELEASE_ORDER(3, "Actualizar el resultado al liberar unaorden desde el dispatcher", null, null),

        CANCEL_ORDER(4, "Acción para cambiar el estado de la orden como entregada", null, null),
        DELIVER_ORDER(4, "Acción para cambiar el estado de la orden como entregada", null, null),
        READY_PICKUP_ORDER(4, "Acción para cambiar el estado de la orden como lista para recoger", null, null),

        ON_STORE_ORDER(2, "Acción para actualizar el estado en tienda", "16","06"),
        ON_ROUTE_ORDER(5, "Acción para actualizar el estado en tienda","19", "09"),
        PICK_ORDER(5, "Acción para actualizar el estado en tienda", null, null),
        PREPARE_ORDER(5, "Acción para actualizar el estado en tienda", "18", "08"),
        ASSIGN_ORDER(5, "Acción para actualizar el estado en tienda","17", "07"),
        READY_DELIVER_ORDER(5, "Acción para actualizar el estado en tienda", "16","06"),
        ARRIVE_ORDER(5, "Acción para actualizar el estado en tienda", null, null),
        REJECT_ORDER(5, "Acción para actualizar el estado en tienda", null, null),

        NONE(0, "", "-1", "-1");

        private Integer code;
        private String description;
        private String orderSuccessStatusCode;
        private String orderErrorStatusCode;

        ActionOrder(Integer code, String description, String orderSuccessStatusCode, String orderErrorStatusCode) {
            this.code = code;
            this.description = description;
            this.orderSuccessStatusCode = orderSuccessStatusCode;
            this.orderErrorStatusCode = orderErrorStatusCode;
        }

        public Integer getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public String getOrderSuccessStatusCode() {
            return orderSuccessStatusCode;
        }

        public String getOrderErrorStatusCode() {
            return orderErrorStatusCode;
        }

        public static ActionOrder getByName(String name) {

            return EnumUtils.getEnumList(ActionOrder.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(name))
                    .findFirst()
                    .orElse(NONE);
        }
    }

    enum OrderStatus {

        SUCCESS_FULFILLMENT_PROCESS("00"),

        ERROR_INSERT_TRACKER("01"),
        ERROR_INSERT_INKAVENTA("02"),
        ERROR_RESERVED_ORDER("03"),
        ERROR_RELEASE_ORDER("04"),
        ERROR_UPDATE_TRACKER_BILLING("05"),
        ERROR_ON_STORE("06"),
        ERROR_ASSIGNED("07"),
        ERROR_PREPARED("08"),
        ERROR_ON_ROUTE("09"),

        ERROR_ARRIVE("31"),
        ERROR_REJECT("32"),
        ERROR_TO_CANCEL_ORDER("33"),
        ERROR_DELIVER("34"),
        ERROR_PICKUP("35"),
        ERROR_UPDATE("36"),
        CANCELLED_ORDER_ONLINE_PAYMENT("37"),
        DELETED_PENDING_ORDER("38"),
        ERROR_ORDER_CANCELLED_TRACKER("39"),
        
        SUCCESS_RESERVED_ORDER("10"),

        CANCELLED_ORDER("11"),
        DELIVERED_ORDER("12"),
        READY_PICKUP_ORDER("13"),
        RELEASED_ORDER("14"),

        CONFIRMED("15"),
        ON_STORE("16"),
        ASSIGNED("17"),
        PREPARED("18"),
        ON_ROUTE("19"),
        ARRIVED("20"),
        REJECTED("21"),


        NOT_FOUND_CODE("-1"),
        NOT_FOUND_ORDER("-1"),
        NOT_DEFINED_ERROR("-1"),
        NOT_DEFINED_STATUS("-1"),
        NOT_FOUND_ACTION("-1"),
        EMPTY_RESULT_DISPATCHER("-1"),
        EMPTY_RESULT_INKATRACKER("-1");

        private String code;

        OrderStatus(String code) {
            this.code = code;
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

        public String getCode() {
            return code;
        }
    }

    enum Logical {

        Y(true, "1"), N(false, "0");

        private final boolean value;
        private String valueString;

        Logical(boolean value) {
            this.value = value;
        }
        Logical(boolean value, String valueString) {

            this.value = value;
            this.valueString = valueString;
        }

        public String getValueString() {
            return valueString;
        }

        public boolean value() {
            return value;
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

    enum PaymentMethodCode {

        NONE(null), CASH("CASH"), CARD("POS"), ONLINE_PAYMENT("3");

        private final String value;

        PaymentMethodCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static PaymentMethodCode getByValue(String value) {
            return EnumUtils.getEnumList(PaymentMethodCode.class)
                    .stream()
                    .filter(item -> value.equalsIgnoreCase(item.getValue()))
                    .findFirst()
                    .orElse(NONE);
        }
    }
    
    enum OrderTrackerStatusMapper {
    	CANCELLED(OrderStatus.CANCELLED_ORDER, OrderStatus.ERROR_TO_CANCEL_ORDER)
    	, REJECTED(OrderStatus.REJECTED, OrderStatus.ERROR_REJECT)
    	, NOT_DEFINED(OrderStatus.NOT_DEFINED_STATUS, OrderStatus.NOT_DEFINED_ERROR);

        private OrderStatus successStatus;
        private OrderStatus errorStatus;

        public OrderStatus getSuccessStatus() {
            return successStatus;
        }
        
        public OrderStatus getErrorStatus() {
            return errorStatus;
        }

        OrderTrackerStatusMapper(OrderStatus successStatus, OrderStatus errorStatus) {
            this.successStatus = successStatus;
            this.errorStatus = errorStatus;
        }
        
        public static OrderTrackerStatusMapper getByName(String name) {
            return EnumUtils.getEnumList(OrderTrackerStatusMapper.class)
                    .stream()
                    .filter(item -> name.equals(item.name()))
                    .findFirst()
                    .orElse(OrderTrackerStatusMapper.NOT_DEFINED);
        }
    }
}
