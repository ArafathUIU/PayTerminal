package com.arafath.payterminalversion2.data.remote.dto;

public class RefreshRequest {
    public String refreshToken;

    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
