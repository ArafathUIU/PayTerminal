package com.arafath.payterminalversion2.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Upsert;

import com.arafath.payterminalversion2.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    @Upsert
    void upsert(UserEntity user);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    UserEntity getById(String id);

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    LiveData<UserEntity> observeById(String id);

    @Query("SELECT * FROM users LIMIT 1")
    LiveData<UserEntity> observeFirst();

    @Query("DELETE FROM users")
    void deleteAll();
}