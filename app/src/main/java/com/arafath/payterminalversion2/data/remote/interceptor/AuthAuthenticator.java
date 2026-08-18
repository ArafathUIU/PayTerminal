package com.arafath.payterminalversion2.data.remote.interceptor;

import com.arafath.payterminalversion2.data.remote.api.AuthApi;
import com.arafath.payterminalversion2.data.remote.dto.AuthResponse;
import com.arafath.payterminalversion2.data.remote.dto.RefreshRequest;
import com.arafath.payterminalversion2.data.session.TokenStore;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

/**
 * OkHttp authenticator: transparently handles access-token expiry. When a
 * request comes back 401, the refresh token is exchanged for a fresh token
 * pair (single-flight under a lock so parallel 401s cause one refresh), the
 * original request is retried with the new bearer token, and the session is
 * cleared if the refresh itself fails.
 */
@Singleton
public class AuthAuthenticator implements Authenticator {
    private final TokenStore tokenStore;
    private final AuthApi authApi;
    private final Object refreshLock = new Object();

    @Inject
    public AuthAuthenticator(TokenStore tokenStore, AuthApi authApi) {
        this.tokenStore = tokenStore;
        this.authApi = authApi;
    }

    @Override
    public Request authenticate(Route route, Response response) throws IOException {
        synchronized (refreshLock) {
            if (!tokenStore.hasSession()) {
                return null;
            }

            AuthResponse refreshed = refreshTokens();
            if (refreshed == null) {
                tokenStore.clear();
                return null;
            }
        }

        String accessToken = tokenStore.getAccessToken();
        if (accessToken == null) {
            return null;
        }
        return response.request().newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .build();
    }

    private AuthResponse refreshTokens() throws IOException {
        String refreshToken = tokenStore.getRefreshToken();
        if (refreshToken == null) {
            return null;
        }

        retrofit2.Response<AuthResponse> response =
                authApi.refresh(new RefreshRequest(refreshToken)).execute();
        if (!response.isSuccessful() || response.body() == null) {
            return null;
        }

        AuthResponse body = response.body();
        tokenStore.saveTokens(
                body.accessToken,
                body.refreshToken,
                System.currentTimeMillis() + body.expiresIn * 1000L);
        return body;
    }
}