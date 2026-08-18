package com.arafath.payterminalversion2.data.remote.api;

import com.arafath.payterminalversion2.data.remote.dto.RegisterTerminalRequest;
import com.arafath.payterminalversion2.data.remote.dto.TerminalResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface TerminalApi {
    @POST("api/v1/terminals/register")
    Call<TerminalResponse> register(@Body RegisterTerminalRequest request);

    @GET("api/v1/terminals/{id}")
    Call<TerminalResponse> get(@Path("id") String id);

    @POST("api/v1/terminals/{id}/heartbeat")
    Call<TerminalResponse> heartbeat(@Path("id") String id);
}