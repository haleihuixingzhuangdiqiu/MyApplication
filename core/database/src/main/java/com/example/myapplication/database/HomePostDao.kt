package com.example.myapplication.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface HomePostDao {

    @Query("SELECT * FROM home_post_cache ORDER BY postId ASC")
    fun observeAll(): Flow<List<HomePostCacheEntity>>

    @Query("DELETE FROM home_post_cache")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HomePostCacheEntity>)

    @Transaction
    suspend fun replaceAll(items: List<HomePostCacheEntity>) {
        deleteAll()
        insertAll(items)
    }
}
