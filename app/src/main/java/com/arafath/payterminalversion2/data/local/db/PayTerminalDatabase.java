package com.arafath.payterminalversion2.data.local.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.arafath.payterminalversion2.data.local.dao.MerchantDao;
import com.arafath.payterminalversion2.data.local.dao.TerminalDao;
import com.arafath.payterminalversion2.data.local.dao.UserDao;
import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;

@Database(
        entities = {UserEntity.class, MerchantEntity.class, TerminalEntity.class},
        version = 1,
        exportSchema = false)
public abstract class PayTerminalDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract MerchantDao merchantDao();
    public abstract TerminalDao terminalDao();
}