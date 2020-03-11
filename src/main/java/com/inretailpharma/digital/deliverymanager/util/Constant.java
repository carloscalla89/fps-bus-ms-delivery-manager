package com.inretailpharma.digital.deliverymanager.util;

import org.apache.commons.lang3.EnumUtils;

import java.util.Optional;

public interface Constant {


    interface OrderTrackerResponseCode {
        String SUCCESS_CODE = "0";
        String ERROR_CODE = "1";
    }

    interface ApplicationsParameters {
        String ACTIVATED_AUDIT_VALUE = "1";
        String ACTIVATED_AUDIT = "ACTIVATED_AUDIT";

        String ACTIVATED_ORDER_TRACKER_VALUE = "1";
        String ACTIVATED_ORDER_TRACKER = "ACTIVATED_ORDER_TRACKER";
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

    enum ActionOrder {

        ATTEMPT_TRACKER_CREATE(1, "reintento para enviar la orden a un tracker", null, null),
        UPDATE_TRACKER_BILLING(1, "actualizar el BILLING ID(número de pedido diario) a un tracker", null, null),

        ATTEMPT_INSINK_CREATE(2, "reintento para enviar la órden al insink", null, null),
        RELEASE_ORDER(2, "Liberar orden reservada", null, null),

        UPDATE_RELEASE_ORDER(3, "Actualizar el resultado al liberar unaorden desde el dispatcher", null, null),

        CANCEL_ORDER(4, "Acción para cambiar el estado de la orden como entregada", null, null),
        DELIVER_ORDER(4, "Acción para cambiar el estado de la orden como entregada", null, null),
        READY_PICKUP_ORDER(4, "Acción para cambiar el estado de la orden como lista para recoger", null, null),

        ON_STORE_ORDER(5, "Acción para actualizar el estado en tienda", "16","06"),
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
        ERROR_CANCEL("33"),
        ERROR_DELIVER("34"),
        ERROR_PICKUP("35"),
        ERROR_UPDATE("36"),

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
        NOT_FOUND_ACTION("-1");

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

        Y(true), N(false);

        private final boolean value;

        Logical(boolean value) {
            this.value = value;
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
    }

    interface Constans {
        Integer ONE_ATTEMPT = 1;
        String SUCCESS_CODE = "00";
        String NOT_DEFINED_CENTER = "NDC";
        String NOT_DEFINED_COMPANY = "NDC";
        String NOT_DEFINED_SERVICE = "NDS";
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
}