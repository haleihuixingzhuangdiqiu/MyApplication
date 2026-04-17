package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AppMetaDao {

    @Query("SELECT COUNT(*) FROM app_meta")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AppMetaEntity)

    @Query("SELECT value FROM app_meta WHERE `key` = :key LIMIT 1")
    suspend fun getValue(key: String): String?
}
