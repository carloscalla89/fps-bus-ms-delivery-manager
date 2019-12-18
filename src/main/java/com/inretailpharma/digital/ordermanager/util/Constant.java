package com.inretailpharma.digital.ordermanager.util;

import org.apache.commons.lang3.EnumUtils;

import java.util.Optional;

public interface Constant {


    enum OrderStatus {

        FULFILLMENT_PROCESS_SUCCESS("00"),
        ERROR_INSERT_TRACKER("01"),
        ERROR_INSERT_INKAVENTA("02"),
        ERROR_RELEASE_ORDER("03"),

        SUCCESS_TRACKED_BILLED_ORDER("08"),
        SUCCESS_RESERVED_ORDER("09"),
        ERROR_SEND_TRACK_RESERVED_ORDER("10"),
        ERROR_RESERVED_ORDER("11");
        private String code;

        OrderStatus(String code) {
            this.code = code;
        }

        public static OrderStatus getByCode(String code) {
            return EnumUtils.getEnumList(OrderStatus.class)
                    .stream()
                    .filter(item -> item.code.equalsIgnoreCase(code))
                    .findFirst()
                    .orElse(ERROR_INSERT_TRACKER);
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