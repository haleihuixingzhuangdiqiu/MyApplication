package com.example.myapplication.game.adapter

import androidx.annotation.Keep
import com.example.myapplication.database.HomePostCacheEntity

/**
 * 列表项模型：只保留展示相关字段，避免仅 fetchedAt 变化导致整行无效重绘。
 */
@Keep
data class GamePostRowModel(
    val postId: Int,
    val title: String,
    val bodyPreview: String,
    val coverImageUrl: String,
) {
    companion object {
        fun from(entity: HomePostCacheEntity): GamePostRowModel {
            return GamePostRowModel(
                postId = entity.postId,
                title = entity.title,
                bodyPreview = entity.body.replace("\n", " ").take(120),
                coverImageUrl = entity.coverImageUrl,
            )
        }
    }
}
