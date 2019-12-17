package com.inretailpharma.digital.ordermanager.util;

import org.apache.commons.lang3.EnumUtils;

import java.util.Optional;

public interface Constant {

    Integer EARTH_RADIUS = 6371000;
    Integer DS_INKA_TRACKER = 3;
    Integer DS_INKA_TRACKER_LITE = 4;
    String APPLICATION_NAME = "app-name";
    String BREAK_STATUS_ALERT = "M01";
    Long MAX_TIME_IN_QUEUE = 5000L;
    int POSITION_NOT_SETTED = 0;



    interface orderStatus {
        // states to send at insink
        String ERROR_BILLING_PROCESS = "ERROR_BILLING_PROCESS";

        // state to release order
        String ERROR_RELEASE_RESERVED = "ERROR_RELEASE_RESERVED";

        // States to track
        String ERROR_TRACKING_PROCESS = "ERROR_TRACKING_PROCESS";
        String PENDING_TRACKING_PROCESS = "PENDING_TRACKING_PROCESS";
        String SUCCESS_TRACKING_PROCESS = "SUCCESS_TRACKER_PROCESS";

        // STATES TO ASSIGNED SHIPPER
        String SUCCESS_ASSIGNED_SHIPPER = "SUCCESS_ASSIGNED_SHIPPER";
        String ERROR_ASSIGNED_SHIPPER = "ERROR_ASSIGNED_SHIPPER";
        String PENDING_ASSIGNED_SHIPPER = "ERROR_ASSIGNED_SHIPPER";

        // state to send insink and tracker
        String ERROR_ECOMMERCE_PROCESS = "ERROR_ECCOMMERCE_PROCESS";
    }

    interface Integers {

        Integer ZERO = 0;
        Integer ONE = 1;
        Integer TWO = 2;
        Integer FIFTEEN = 15;
        Integer SIXTEEN = 16;
    }

    enum ErrorStatusOrderResponse {
        ERROR_NOT_DEFINED("En canal","00","Error no definido del Ecommerce"),
        ERROR_BILLING_PROCESS("En canal", "02","Error al insertar inkaventa"),
        ERROR_TRACKING_PROCESS("En Tracking","01", "Error al insertar pedido a los tracking"),
        ERROR_ECOMMERCE_PROCESS("En canal","04", "Error al insertar inkaventa y traking");

        private String status;
        private String errorCode;
        private String errorCodeDescription;

        public static ErrorStatusOrderResponse getByValue(String value) {
            return EnumUtils.getEnumList(ErrorStatusOrderResponse.class)
                    .stream()
                    .filter(item -> item.name().equalsIgnoreCase(value))
                    .findFirst()
                    .orElse(ERROR_NOT_DEFINED);
        }

        ErrorStatusOrderResponse(String status,String errorCode, String errorCodeDescription) {
            this.status = status;
            this.errorCode = errorCode;
            this.errorCodeDescription = errorCodeDescription;
        }

        public String getStatus() {
            return status;
        }

        public String getErrorCode() {
            return errorCode;
        }

        public String getErrorCodeDescription() {
            return errorCodeDescription;
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