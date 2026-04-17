package com.example.myapplication.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppMetaEntity::class,
        HomePostCacheEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appMetaDao(): AppMetaDao
    abstract fun homePostDao(): HomePostDao
}
