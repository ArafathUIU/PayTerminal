package com.arafath.payterminalversion2.data;

public class Result<T> {
    public final boolean success;
    public final T data;
    public final String errorMessage;

    private Result(boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<>(true, data, null);
    }

    public static <T> Result<T> error(String errorMessage) {
        return new Result<>(false, null, errorMessage);
    }
}