package com.arafath.payterminalversion2.data.remote.api;

import com.arafath.payterminalversion2.data.remote.dto.AuthResponse;
import com.arafath.payterminalversion2.data.remote.dto.LoginRequest;
import com.arafath.payterminalversion2.data.remote.dto.RefreshRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/v1/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/v1/auth/refresh")
    Call<AuthResponse> refresh(@Body RefreshRequest request);
}