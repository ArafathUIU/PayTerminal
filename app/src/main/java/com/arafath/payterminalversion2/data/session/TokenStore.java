package com.arafath.payterminalversion2.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.hilt.android.qualifiers.ApplicationContext;

/**
 * Vault for the JWT access + refresh tokens. Tokens are secrets, so they are NOT
 * stored in the plaintext Room database; they live in Keystore-backed encrypted
 * preferences (AES256-GCM master key, SIV-encrypted keys/values).
 *
 * Exposes an observable {@link #sessionActive()} so the session gate can react
 * to token loss (e.g. a failed token refresh) instead of staying on a stale
 * logged-in screen.
 */
@Singleton
public class TokenStore {
    private static final String FILE_NAME = "payterminal_tokens";
    private static final String KEY_ACCESS = "access_token";
    private static final String KEY_REFRESH = "refresh_token";
    private static final String KEY_EXPIRES_AT = "expires_at";

    private final SharedPreferences prefs;
    private final MutableLiveData<Boolean> sessionActive;

    @Inject
    public TokenStore(@ApplicationContext Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    context,
                    FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (GeneralSecurityException | IOException e) {
            throw new IllegalStateException("Failed to initialise encrypted token store", e);
        }
        sessionActive = new MutableLiveData<>(getRefreshToken() != null);
    }

    public LiveData<Boolean> sessionActive() {
        return sessionActive;
    }

    public synchronized String getAccessToken() {
        return prefs.getString(KEY_ACCESS, null);
    }

    public synchronized String getRefreshToken() {
        return prefs.getString(KEY_REFRESH, null);
    }

    public synchronized boolean hasSession() {
        return getRefreshToken() != null;
    }

    public synchronized void saveTokens(String accessToken, String refreshToken, long expiresAtMillis) {
        prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .putLong(KEY_EXPIRES_AT, expiresAtMillis)
                .apply();
        sessionActive.postValue(true);
    }

    public synchronized void clear() {
        prefs.edit().clear().apply();
        sessionActive.postValue(false);
    }
}