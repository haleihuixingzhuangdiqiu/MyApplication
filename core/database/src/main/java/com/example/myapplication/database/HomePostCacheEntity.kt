package com.example.myapplication.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "home_post_cache")
data class HomePostCacheEntity(
    @PrimaryKey val postId: Int,
    val title: String,
    val body: String,
    val coverImageUrl: String,
    val fetchedAtMillis: Long,
)
