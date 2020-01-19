package com.inretailpharma.digital.deliverymanager.util;

import org.apache.commons.lang3.EnumUtils;

import java.util.Optional;

public interface Constant {

    interface InsinkErrorCode {
        String CODE_ERROR_GENERAL = "E-0";
        String CODE_ERROR_CLIENT_CONNECTION = "C-0";
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

        ATTEMPT_TRACKER_CREATE(1, "reintento para enviar la orden a un tracker"),
        UPDATE_TRACKER_BILLING(1, "actualizar el BILLING ID(número de pedido diario) a un tracker"),

        ATTEMPT_INSINK_CREATE(2, "reintento para enviar la órden al insink"),
        RELEASE_ORDER(2, "Liberar orden reservada"),

        UPDATE_RELEASE_ORDER(3, ""),

        CANCEL_ORDER(4, "Acción para cambiar el estado de la orden como entregada"),
        DELIVER_ORDER(4, "Acción para cambiar el estado de la orden como entregada"),
        READY_PICKUP_ORDER(4, "Acción para cambiar el estado de la orden como lista para recoger"),

        NONE(0, "");

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

    enum OrderStatus {

        SUCCESS_FULFILLMENT_PROCESS("00"),

        ERROR_INSERT_TRACKER("01"),
        ERROR_INSERT_INKAVENTA("02"),
        ERROR_RESERVED_ORDER("03"),
        ERROR_RELEASE_ORDER("04"),
        ERROR_UPDATE_TRACKER_BILLING("05"),
        ERROR_UPDATE_ORDER("06"),


        SUCCESS_RESERVED_ORDER("10"),

        CANCELLED_ORDER("11"),
        DELIVERED_ORDER("12"),
        READY_PICKUP_ORDER("13"),
        RELEASED_ORDER("14"),

        PENDING_CANCEL_ORDER("21"),
        PENDING_DELIVERY_ORDER("22"),
        PENDING_READY_PICKUP_ORDER("23"),
        PENDING_RELEASE_ORDER("24"),

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