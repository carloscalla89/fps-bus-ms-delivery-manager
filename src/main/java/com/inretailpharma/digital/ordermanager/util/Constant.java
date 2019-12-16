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
        String PENDING_ECOMMERCE_PROCESS = "PENDING_ECOMMERCE";
        String SUCCESS_ORDER_TO_TRACK = "SUCCESS_TRACKER";
        String SUCCESS_ASSIGNED_SHIPPER = "SUCCESS_ASSIGNED_SHIPPER";
        String ERROR_ASSIGNED_SHIPPER = "ERROR_ASSIGNED_SHIPPER";
    }

    interface Integers {

        Integer ZERO = 0;
        Integer ONE = 1;
        Integer TWO = 2;
        Integer FIFTEEN = 15;
        Integer SIXTEEN = 16;
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

    interface Firebase {

        String ORDER_STATUS_PATH_LISTENER = "orderStatusDto/statusName";
        String GROUP_PATH_LISTENER = "group/name";
        String ADDRESS_PATH_LISTENER = "address";
        String ORDER_MOTORIZED_PATH_LISTENER = "motorizedId";
        String USER_STATUS_PATH_LISTENER = "status/statusName";

        String FIREBASE_STATUS_PATH = "status";
        String FIREBASE_ALERT_PATH = "alerts";
        String FIREBASE_DEVICE_PATH = "device";
        String FIREBASE_GROUP_PATH = "group";

        String FIREBASE_ETAP_PATH = "etap";

        String ORDER_SHELF_PATH= "shelf";

        String ORDER_SHELF_LOCK_CODE = "lockCode";
        String ORDER_SHELF_PACK_CODE = "packCode";

        String ORDER_SCHEDULED_PUSH_NOTIFICATION_STATUS = "pushNotificationStatus";
        String ORDER_PUSH_SEND_SCHEDULE_PUSH = "SEND_SCHEDULE_PUSH";
    }


    interface MailTemplate {

        String TH_START = "<th style=\"border: 1px solid black;padding: 5px;\" >";
        String TD_START_LEFT = "<td style=\"border: 1px solid black;padding: 5px;\" >";
        String TD_START_RIGHT = "<td style=\"border: 1px solid black;padding: 5px;text-align:right;\" >";
        String TD_START_CENTER = "<td style=\"border: 1px solid black;padding: 5px;text-align:center;\" >";

        String TD_WIDTH_IMAGE = "<td width=\"20%\">";
        String TD_WIDTH_NAME = "<td width=\"50%\" style=\"border-bottom: 1px solid #c1c2c9;\">";
        String TD_WIDTH_PRICE = "<td width=\"30%\" style=\"border-bottom: 1px solid #c1c2c9;\">";
        String IMG_PRODUCT = "<img width=\"180\" src=\"";
        String P_NAME = "<h4 style=\"margin-bottom: 5px;\"><b>";
        String P_PRESENTATION = "<h5 style=\"color: #a6a7b1; margin-top: 10px; font-weight: 500;\">";
        String P_PRICE = "<h3 style=\"color: #009540; font-weight: 500;\">S/ ";
        String X = "X";

        String MSG_SCHEDULED = "Entregaremos su pedido el ";
        String MSG_CONFIRMED = "Entregaremos su pedido entre ";

        String GIF_CAR = "\"https://s3-us-west-2.amazonaws.com/inkafarmaproductimages/email/carrito-listo.gif\"";
        String IMG_LOGO = "\"https://s3-us-west-2.amazonaws.com/inkafarmaproductimages/email/logo-01.png\"";
        String BTN_ORDER = "\"https://s3-us-west-2.amazonaws.com/inkafarmaproductimages/email/boton-01.png\"";
        String MAILTO = "\"mailto:ayuda@inkafarmadigital.pe?subject=[Confirmación%20Orden]\"";

        String DEFAULT_IMAGE = "https://s3-us-west-2.amazonaws.com/inkafarmaproductimages/newimages/imagen_default.png";
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