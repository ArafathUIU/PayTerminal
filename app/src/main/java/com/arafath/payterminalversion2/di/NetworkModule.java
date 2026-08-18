package com.arafath.payterminalversion2.di;

import com.arafath.payterminalversion2.BuildConfig;
import com.arafath.payterminalversion2.data.remote.api.AuthApi;
import com.arafath.payterminalversion2.data.remote.api.MerchantApi;
import com.arafath.payterminalversion2.data.remote.api.TerminalApi;
import com.arafath.payterminalversion2.data.remote.interceptor.AuthAuthenticator;
import com.arafath.payterminalversion2.data.remote.interceptor.AuthInterceptor;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor, AuthAuthenticator authAuthenticator) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .authenticator(authAuthenticator)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            // BASIC logs method + URL + status without leaking token bodies in logcat.
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(logging);
        }

        return builder.build();
    }

    @Provides
    @Singleton
    static Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
    }

    @Provides
    @Singleton
    static AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }

    @Provides
    @Singleton
    static MerchantApi provideMerchantApi(Retrofit retrofit) {
        return retrofit.create(MerchantApi.class);
    }

    @Provides
    @Singleton
    static TerminalApi provideTerminalApi(Retrofit retrofit) {
        return retrofit.create(TerminalApi.class);
    }
}