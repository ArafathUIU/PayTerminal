package com.arafath.payterminalversion2.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Upsert;

import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;

import java.util.List;

@Dao
public interface PaymentTransactionDao {

    @Upsert
    void upsert(PaymentTransactionEntity transaction);

    @Update
    void update(PaymentTransactionEntity transaction);

    @Query("SELECT * FROM payment_transactions WHERE merchant_id = :merchantId ORDER BY created_at DESC")
    LiveData<List<PaymentTransactionEntity>> observeByMerchantId(String merchantId);

    @Query("SELECT * FROM payment_transactions WHERE merchant_id = :merchantId ORDER BY created_at DESC LIMIT :limit")
    LiveData<List<PaymentTransactionEntity>> observeRecentByMerchantId(String merchantId, int limit);

    @Query("SELECT * FROM payment_transactions WHERE id = :id LIMIT 1")
    PaymentTransactionEntity getById(String id);

    @Query("SELECT * FROM payment_transactions WHERE id = :id LIMIT 1")
    LiveData<PaymentTransactionEntity> observeById(String id);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payment_transactions WHERE merchant_id = :merchantId AND status = 'SUCCESS'")
    LiveData<Long> observeTotalSuccessfulAmount(String merchantId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payment_transactions WHERE merchant_id = :merchantId AND status = 'SUCCESS' AND created_at >= :since")
    LiveData<Long> observeTotalSuccessfulAmountSince(String merchantId, long since);

    @Query("SELECT COUNT(*) FROM payment_transactions WHERE merchant_id = :merchantId")
    LiveData<Integer> observeTransactionCount(String merchantId);

    @Query("SELECT COUNT(*) FROM payment_transactions WHERE merchant_id = :merchantId AND status = 'SUCCESS'")
    LiveData<Integer> observeSuccessfulCount(String merchantId);

    @Query("SELECT COUNT(*) FROM payment_transactions WHERE merchant_id = :merchantId AND status = 'FAILED'")
    LiveData<Integer> observeFailedCount(String merchantId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payment_transactions WHERE merchant_id = :merchantId AND status = 'REFUNDED'")
    LiveData<Long> observeRefundedAmount(String merchantId);

    @Query("DELETE FROM payment_transactions WHERE merchant_id = :merchantId")
    void deleteByMerchantId(String merchantId);
}