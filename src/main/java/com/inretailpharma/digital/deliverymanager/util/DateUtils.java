package com.inretailpharma.digital.deliverymanager.util;

import org.apache.commons.validator.GenericValidator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    private static final String TIME_TEMPLATE = "HH:mm:ss";
    private static final String TIME_TEMPLATE_HOUR_MINUTE = "HH:mm";
    private static final String DATE_TEMPLATE = "yyyy-MM-dd";
    private static final String DATETIME_TEMPLATE = "yyyy-MM-dd HH:mm:ss";
    private static final String DATETIME_TEMPLATE_DDMMYY_AMPM = "dd-MM-yy hh:mm a";
    private static final String DATE_TEMPLATE_V2 = "dd-MM-yyyy";

    public static boolean validFormatDateTimeFormat(String dateTime) {

        return GenericValidator.isDate(dateTime, DATETIME_TEMPLATE, true);

    }

    public static String getFormatDateTimeTemplate() {
        return DATETIME_TEMPLATE;
    }

    public static String getLocalTimeWithFormat(LocalTime localtime) {
        return localtime.format(DateTimeFormatter.ofPattern(TIME_TEMPLATE));
    }

    public static String getLocalDateTimeWithFormat(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DATETIME_TEMPLATE));
    }

    public static String getLocalDateWithFormat(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DATE_TEMPLATE));
    }

    public static LocalDateTime getLocalDateTimeByInputString(String actionDate) {
        return Optional
                .ofNullable(actionDate)
                .filter(DateUtils::validFormatDateTimeFormat)
                .map(DateUtils::getLocalDateTimeFromStringWithFormat)
                .orElseGet(DateUtils::getLocalDateTimeObjectNow);
    }

    public static LocalTime getLocalTimeWithValidFormat(String localtime) {

        if (GenericValidator.isDate(localtime, TIME_TEMPLATE_HOUR_MINUTE, true)) {
            return LocalTime.parse(localtime, DateTimeFormatter.ofPattern(TIME_TEMPLATE_HOUR_MINUTE));
        } else if (GenericValidator.isDate(localtime, TIME_TEMPLATE, true)) {
            return LocalTime.parse(localtime, DateTimeFormatter.ofPattern(TIME_TEMPLATE));
        } else {
            return null;
        }
    }

    public static String getLocalDateTimeFormatUTC(String localDateTime) {

        return LocalDateTime.parse(localDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toString();

    }

    public static String getLocalDateTimeWithFormatDDMMYY_AMPM(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DATETIME_TEMPLATE_DDMMYY_AMPM));
    }

    public static LocalDateTime getLocalDateTimeFromStringWithFormat(String localDateTime) {
    	try {
    		return LocalDateTime.parse(localDateTime, DateTimeFormatter.ofPattern(DATETIME_TEMPLATE));
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
        return null;
    }

    public static LocalDate getLocalDateFromStringWithFormatV2(String localDateTime) {
        return LocalDate.parse(localDateTime, DateTimeFormatter.ofPattern(DATE_TEMPLATE_V2));
    }

    public static LocalDateTime getLocalDateTimeObjectNow() {


        return LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_TEMPLATE)),
                DateTimeFormatter.ofPattern(DATETIME_TEMPLATE));
    }

    public static String getLocalDateTimeNow() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATETIME_TEMPLATE));
    }


    public static LocalDate getLocalDateFromStringDate(String localDate) {
        return LocalDate.parse(localDate, DateTimeFormatter.ofPattern(DATE_TEMPLATE));
    }


    
    public static Long getCurrentDateMillis() {
    	return System.currentTimeMillis();
    }

}
