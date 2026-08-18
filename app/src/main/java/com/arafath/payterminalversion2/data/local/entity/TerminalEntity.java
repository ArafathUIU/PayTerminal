package com.arafath.payterminalversion2.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "terminals")
public class TerminalEntity {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "id")
    public String id;

    @ColumnInfo(name = "merchant_id")
    public String merchantId;

    @ColumnInfo(name = "code")
    public String code;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "status")
    public String status;

    @ColumnInfo(name = "paired_at")
    public String pairedAt;

    @ColumnInfo(name = "last_heartbeat_at")
    public String lastHeartbeatAt;
}