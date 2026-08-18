package com.arafath.payterminalversion2.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "merchants")
public class MerchantEntity {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "merchant_id")
    public String merchantId;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "business_name")
    public String businessName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "phone")
    public String phone;
}