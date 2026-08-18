package com.arafath.payterminalversion2.di;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * Marks the OkHttp client (and the AuthApi built on it) that carries no bearer
 * interceptor and no 401 authenticator. Used only for the token-refresh call so
 * a 401 from a revoked refresh token cannot re-enter the authenticator.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
public @interface Authless {
}