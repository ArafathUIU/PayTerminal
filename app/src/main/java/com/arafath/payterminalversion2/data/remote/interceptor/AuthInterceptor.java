package com.arafath.payterminalversion2.data.remote.interceptor;

import com.arafath.payterminalversion2.data.session.TokenStore;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Adds the JWT bearer token to every request that has one. The token is read
 * from the encrypted TokenStore at request time, so freshly refreshed tokens
 * are picked up automatically.
 */
@Singleton
public class AuthInterceptor implements Interceptor {
    private final TokenStore tokenStore;

    @Inject
    public AuthInterceptor(TokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String accessToken = tokenStore.getAccessToken();
        if (accessToken != null && request.header("Authorization") == null) {
            request = request.newBuilder()
                    .header("Authorization", "Bearer " + accessToken)
                    .build();
        }
        return chain.proceed(request);
    }
}