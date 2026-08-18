package com.arafath.payterminalversion2.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.arafath.payterminalversion2.data.local.entity.TerminalEntity;

@Dao
public interface TerminalDao {
    @Upsert
    void upsert(TerminalEntity terminal);

    @Query("SELECT * FROM terminals WHERE id = :id LIMIT 1")
    TerminalEntity getById(String id);

    @Query("SELECT * FROM terminals LIMIT 1")
    TerminalEntity getFirst();

    @Query("SELECT * FROM terminals LIMIT 1")
    LiveData<TerminalEntity> observeFirst();

    @Query("DELETE FROM terminals")
    void deleteAll();
}