package com.arafath.payterminalversion2.data.repository;

import androidx.lifecycle.LiveData;

import com.arafath.payterminalversion2.data.local.dao.PaymentTransactionDao;
import com.arafath.payterminalversion2.data.local.entity.PaymentTransactionEntity;
import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;
import com.arafath.payterminalversion2.data.local.entity.UserEntity;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Local payment simulator. Processes payments on-device and persists the
 * resulting transaction in Room, producing a realistic state timeline so the
 * UI can animate through Initiated -> Pending -> Processing -> Success/Failed.
 */
@Singleton
public class PaymentRepository {
    private final PaymentTransactionDao dao;

    @Inject
    public PaymentRepository(PaymentTransactionDao dao) {
        this.dao = dao;
    }

    public LiveData<List<PaymentTransactionEntity>> observeRecent(String merchantId, int limit) {
        return dao.observeRecentByMerchantId(merchantId, limit);
    }

    public LiveData<List<PaymentTransactionEntity>> observeAll(String merchantId) {
        return dao.observeByMerchantId(merchantId);
    }

    public LiveData<Long> observeTodayTotal(String merchantId) {
        long startOfDay = startOfToday();
        return dao.observeTotalSuccessfulAmountSince(merchantId, startOfDay);
    }

    public LiveData<Integer> observeTransactionCount(String merchantId) {
        return dao.observeTransactionCount(merchantId);
    }

    public LiveData<Integer> observeSuccessfulCount(String merchantId) {
        return dao.observeSuccessfulCount(merchantId);
    }

    public LiveData<Integer> observeFailedCount(String merchantId) {
        return dao.observeFailedCount(merchantId);
    }

    public LiveData<Long> observeRefundedAmount(String merchantId) {
        return dao.observeRefundedAmount(merchantId);
    }

    public PaymentTransactionEntity getById(String id) {
        return dao.getById(id);
    }

    public LiveData<PaymentTransactionEntity> observeById(String id) {
        return dao.observeById(id);
    }

    /**
     * Simulates processing a payment. Returns the persisted transaction via the
     * callback once the "processor" responds.
     *
     * @param amountPaise amount in minor units (paise)
     * @param method      CARD, QR or WALLET
     * @param maskedRef   masked card number, wallet number or QR reference
     * @param user        session user (for merchant id)
     * @param terminal    session terminal (for terminal id/code)
     * @param shouldFail  simulates a declined/failed payment when true
     * @param onResult    receives the final transaction
     */
    public void process(
            long amountPaise,
            String method,
            String maskedRef,
            UserEntity user,
            TerminalEntity terminal,
            boolean shouldFail,
            java.util.function.Consumer<PaymentTransactionEntity> onResult) {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            PaymentTransactionEntity tx = new PaymentTransactionEntity();
            tx.id = txId();
            tx.merchantId = user.merchantId;
            tx.terminalId = terminal.id;
            tx.terminalCode = terminal.code;
            tx.amountPaise = amountPaise;
            tx.currency = "BDT";
            tx.method = method;
            tx.status = shouldFail ? PaymentTransactionEntity.STATUS_FAILED : PaymentTransactionEntity.STATUS_SUCCESS;
            tx.reference = reference();
            tx.cardMasked = maskedRef;
            tx.createdAt = now;
            tx.processedAt = now;

            sleep(1400); // simulated processor round-trip
            dao.upsert(tx);
            onResult.accept(tx);
        }, "payment-simulator").start();
    }

    public void refund(
            PaymentTransactionEntity original,
            long amountPaise,
            String reason,
            UserEntity user,
            java.util.function.Consumer<PaymentTransactionEntity> onResult) {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            original.status = PaymentTransactionEntity.STATUS_REFUNDED;
            original.refundedAt = now;
            original.refundReason = reason;
            // The refunded amount is tracked as a negative line derived from the original.
            sleep(1000);
            dao.update(original);
            onResult.accept(original);
        }, "refund-simulator").start();
    }

    public void deleteAllFor(String merchantId) {
        dao.deleteByMerchantId(merchantId);
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String txId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    private static String reference() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private static long startOfToday() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now();
        java.time.ZonedDateTime start = now.toLocalDate().atStartOfDay(now.getZone());
        return start.toInstant().toEpochMilli();
    }
}