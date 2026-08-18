package com.arafath.payterminalversion2.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.arafath.payterminalversion2.data.local.entity.MerchantEntity;

@Dao
public interface MerchantDao {
    @Upsert
    void upsert(MerchantEntity merchant);

    @Query("SELECT * FROM merchants WHERE merchant_id = :merchantId LIMIT 1")
    MerchantEntity getById(String merchantId);

    @Query("SELECT * FROM merchants WHERE merchant_id = :merchantId LIMIT 1")
    LiveData<MerchantEntity> observeById(String merchantId);

    @Query("DELETE FROM merchants")
    void deleteAll();
}