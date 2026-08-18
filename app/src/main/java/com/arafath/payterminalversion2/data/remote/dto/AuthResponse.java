package com.arafath.payterminalversion2.data.remote.dto;

public class AuthResponse {
    public String accessToken;
    public String refreshToken;
    public int expiresIn;
    public UserDto user;
}
