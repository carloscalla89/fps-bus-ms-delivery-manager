package com.inretailpharma.digital.deliverymanager.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ObjectUtil {

    public static String objectToJson(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception ex) {
            log.error("objectToJson: ", ex.getMessage());
        }
        return null;
    }

    public static <T> T jsonToObject(String json, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(json, valueType);
        } catch (Exception ex) {
            log.error("jsonToObject: ", ex.getMessage());
        }
        return null;
    }
}
