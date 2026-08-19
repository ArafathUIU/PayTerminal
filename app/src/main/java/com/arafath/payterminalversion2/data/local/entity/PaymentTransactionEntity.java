package com.arafath.payterminalversion2.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "payment_transactions")
public class PaymentTransactionEntity {

    // Statuses
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";

    // Methods
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_QR = "QR";
    public static final String METHOD_WALLET = "WALLET";

    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "merchant_id")
    public String merchantId;

    @ColumnInfo(name = "terminal_id")
    public String terminalId;

    @ColumnInfo(name = "terminal_code")
    public String terminalCode;

    @ColumnInfo(name = "amount")
    public long amountPaise;

    @ColumnInfo(name = "currency")
    public String currency;

    @ColumnInfo(name = "method")
    public String method;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "reference")
    public String reference;

    @ColumnInfo(name = "card_masked")
    public String cardMasked;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "processed_at")
    public long processedAt;

    @ColumnInfo(name = "refunded_at")
    public long refundedAt;

    @ColumnInfo(name = "refund_reason")
    public String refundReason;
}