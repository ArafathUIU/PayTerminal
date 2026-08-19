package com.arafath.payterminalversion2.data.repository;

import androidx.lifecycle.LiveData;

import com.arafath.payterminalversion2.data.Result;
import com.arafath.payterminalversion2.data.local.dao.MerchantDao;
import com.arafath.payterminalversion2.data.local.dao.TerminalDao;
import com.arafath.payterminalversion2.data.local.dao.UserDao;
import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;
import com.arafath.payterminalversion2.data.remote.ApiErrorParser;
import com.arafath.payterminalversion2.data.remote.api.AuthApi;
import com.arafath.payterminalversion2.data.remote.api.MerchantApi;
import com.arafath.payterminalversion2.data.remote.dto.AuthResponse;
import com.arafath.payterminalversion2.data.remote.dto.LoginRequest;
import com.arafath.payterminalversion2.data.remote.dto.MerchantRegistrationResponse;
import com.arafath.payterminalversion2.data.remote.dto.RegisterMerchantRequest;
import com.arafath.payterminalversion2.data.session.TokenStore;
import com.arafath.payterminalversion2.di.IoExecutor;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Singleton
public class AuthRepository {
    private final AuthApi authApi;
    private final MerchantApi merchantApi;
    private final TokenStore tokenStore;
    private final UserDao userDao;
    private final MerchantDao merchantDao;
    private final TerminalDao terminalDao;
    private final ApiErrorParser errorParser;
    private final Executor ioExecutor;

    @Inject
    public AuthRepository(
            AuthApi authApi,
            MerchantApi merchantApi,
            TokenStore tokenStore,
            UserDao userDao,
            MerchantDao merchantDao,
            TerminalDao terminalDao,
            ApiErrorParser errorParser,
            @IoExecutor Executor ioExecutor) {
        this.authApi = authApi;
        this.merchantApi = merchantApi;
        this.tokenStore = tokenStore;
        this.userDao = userDao;
        this.merchantDao = merchantDao;
        this.terminalDao = terminalDao;
        this.errorParser = errorParser;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<UserEntity> observeUser() {
        return userDao.observeFirst();
    }

    public LiveData<MerchantEntity> observeMerchant(String merchantId) {
        return merchantDao.observeById(merchantId);
    }

    public boolean hasSession() {
        return tokenStore.hasSession();
    }

    public void login(String email, String password, Consumer<Result<AuthResponse>> onResult) {
        authApi.login(new LoginRequest(email, password)).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    persistLogin(response.body());
                    onResult.accept(Result.ok(response.body()));
                } else {
                    onResult.accept(Result.error(errorParser.messageFrom(null, response)));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                onResult.accept(Result.error(errorParser.messageFrom(t, null)));
            }
        });
    }

    public void register(
            String name,
            String businessName,
            String email,
            String password,
            String phone,
            Consumer<Result<AuthResponse>> onResult) {
        merchantApi.register(new RegisterMerchantRequest(name, businessName, email, password, phone))
                .enqueue(new Callback<MerchantRegistrationResponse>() {
                    @Override
                    public void onResponse(Call<MerchantRegistrationResponse> call, Response<MerchantRegistrationResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            MerchantRegistrationResponse merchant = response.body();
                            ioExecutor.execute(() -> {
                                MerchantEntity entity = new MerchantEntity();
                                entity.merchantId = merchant.merchantId;
                                entity.businessName = merchant.businessName;
                                entity.email = merchant.email;
                                entity.name = name;
                                entity.phone = phone;
                                merchantDao.upsert(entity);
                            });

                            // Registration does not return tokens; sign the owner in.
                            login(email, password, onResult);
                        } else {
                            onResult.accept(Result.error(errorParser.messageFrom(null, response)));
                        }
                    }

                    @Override
                    public void onFailure(Call<MerchantRegistrationResponse> call, Throwable t) {
                        onResult.accept(Result.error(errorParser.messageFrom(t, null)));
                    }
                });
    }

    public void logout() {
        ioExecutor.execute(() -> {
            tokenStore.clear();
            userDao.deleteAll();
            merchantDao.deleteAll();
            terminalDao.deleteAll();
        });
    }

    private void persistLogin(AuthResponse auth) {
        ioExecutor.execute(() -> {
            tokenStore.saveTokens(
                    auth.accessToken,
                    auth.refreshToken,
                    System.currentTimeMillis() + auth.expiresIn * 1000L);

            UserEntity user = new UserEntity();
            user.id = auth.user.id;
            user.name = auth.user.name;
            user.email = auth.user.email;
            user.role = auth.user.role;
            user.merchantId = auth.user.merchantId;
            userDao.upsert(user);
        });
    }
}