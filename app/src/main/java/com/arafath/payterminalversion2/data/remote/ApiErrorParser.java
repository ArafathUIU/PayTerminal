package com.arafath.payterminalversion2.data.remote;

import com.arafath.payterminalversion2.data.remote.dto.ApiError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Response;

/**
 * Converts a failed Retrofit call into a human-readable message, preferring the
 * backend's {code, message, detail} error contract when the body parses.
 */
@Singleton
public class ApiErrorParser {
    private final Gson gson;

    @Inject
    public ApiErrorParser(Gson gson) {
        this.gson = gson;
    }

    public String messageFrom(Throwable throwable, Response<?> response) {
        if (response != null) {
            ApiError error = parseErrorBody(response);
            if (error != null && error.message != null && !error.message.isEmpty()) {
                return error.message;
            }
            if (error != null && error.code != null) {
                return "Request failed: " + error.code;
            }
            return "Request failed with status " + response.code();
        }
        if (throwable instanceof IOException) {
            return "Cannot reach the server. Check that the backend is running.";
        }
        if (throwable != null && throwable.getMessage() != null) {
            return throwable.getMessage();
        }
        return "Unexpected error";
    }

    private ApiError parseErrorBody(Response<?> response) {
        try {
            if (response.errorBody() == null) {
                return null;
            }
            String body = response.errorBody().string();
            return body.isEmpty() ? null : gson.fromJson(body, ApiError.class);
        } catch (IOException | JsonSyntaxException e) {
            return null;
        }
    }
}