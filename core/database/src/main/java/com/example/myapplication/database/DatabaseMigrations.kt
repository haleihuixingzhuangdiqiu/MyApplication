package com.example.myapplication.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `home_post_cache` (
                    `postId` INTEGER NOT NULL,
                    `title` TEXT NOT NULL,
                    `body` TEXT NOT NULL,
                    `coverImageUrl` TEXT NOT NULL,
                    `fetchedAtMillis` INTEGER NOT NULL,
                    PRIMARY KEY(`postId`)
                )
                """.trimIndent(),
            )
        }
    }
}
