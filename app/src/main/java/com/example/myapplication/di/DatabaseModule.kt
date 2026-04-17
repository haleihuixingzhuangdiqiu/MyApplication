package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.database.AppDatabase
import com.example.myapplication.database.AppMetaDao
import com.example.myapplication.database.DatabaseMigrations
import com.example.myapplication.database.HomePostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "my_application.db")
            .addMigrations(DatabaseMigrations.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideHomePostDao(db: AppDatabase): HomePostDao = db.homePostDao()

    @Provides
    @Singleton
    fun provideAppMetaDao(db: AppDatabase): AppMetaDao = db.appMetaDao()
}
