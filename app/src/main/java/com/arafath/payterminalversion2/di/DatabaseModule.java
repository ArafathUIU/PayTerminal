package com.arafath.payterminalversion2.di;

import android.content.Context;

import androidx.room.Room;

import com.arafath.payterminalversion2.data.local.dao.MerchantDao;
import com.arafath.payterminalversion2.data.local.dao.PaymentTransactionDao;
import com.arafath.payterminalversion2.data.local.dao.TerminalDao;
import com.arafath.payterminalversion2.data.local.dao.UserDao;
import com.arafath.payterminalversion2.data.local.db.PayTerminalDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    static PayTerminalDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, PayTerminalDatabase.class, "payterminal.db")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    static UserDao provideUserDao(PayTerminalDatabase database) {
        return database.userDao();
    }

    @Provides
    static MerchantDao provideMerchantDao(PayTerminalDatabase database) {
        return database.merchantDao();
    }

    @Provides
    static TerminalDao provideTerminalDao(PayTerminalDatabase database) {
        return database.terminalDao();
    }

    @Provides
    static PaymentTransactionDao providePaymentTransactionDao(PayTerminalDatabase database) {
        return database.paymentTransactionDao();
    }
}