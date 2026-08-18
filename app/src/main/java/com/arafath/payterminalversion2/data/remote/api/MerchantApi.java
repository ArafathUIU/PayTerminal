package com.arafath.payterminalversion2.data.remote.api;

import com.arafath.payterminalversion2.data.remote.dto.MerchantRegistrationResponse;
import com.arafath.payterminalversion2.data.remote.dto.RegisterMerchantRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MerchantApi {
    @POST("api/v1/merchants/register")
    Call<MerchantRegistrationResponse> register(@Body RegisterMerchantRequest request);
}