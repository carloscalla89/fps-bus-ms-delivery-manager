package com.inretailpharma.digital.deliverymanager.mangepartner.util;

public class GenericResponse<T> {
    private T generic;
    private String statusCode;

    public T getGeneric() {
        return generic;
    }

    public void setGeneric(T generic) {
        this.generic = generic;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }
}