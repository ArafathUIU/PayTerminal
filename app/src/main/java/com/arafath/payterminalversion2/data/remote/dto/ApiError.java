package com.arafath.payterminalversion2.data.remote.dto;

public class ApiError {
    public String code;
    public String message;
    public Object detail;

    public String getDetailText() {
        if (detail instanceof String) {
            return (String) detail;
        }
        return detail == null ? null : detail.toString();
    }
}