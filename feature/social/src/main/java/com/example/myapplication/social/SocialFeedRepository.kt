package com.example.myapplication.social

import com.example.myapplication.database.AppMetaDao
import com.example.myapplication.database.AppMetaEntity
import com.example.myapplication.network.api.JsonPlaceholderApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 关注流演示：网络拉取（不入库），关注 ID 集持久化到 `app_meta`。
 * 列表 + 搜索过滤 + 关注切换。
 */
@Singleton
class SocialFeedRepository @Inject constructor(
    private val api: JsonPlaceholderApi,
    private val appMetaDao: AppMetaDao,
) {

    suspend fun fetchFeed(): Result<List<SocialFeedEntry>> = withContext(Dispatchers.IO) {
        try {
            val remote = api.listPosts()
            val rows = remote.map { dto ->
                SocialFeedEntry(
                    id = dto.id,
                    title = dto.title.trim().take(48),
                    anchorLine = "主播 UID ${dto.userId} · 条目 #${dto.id}",
                    coverUrl = "https://picsum.photos/seed/social-${dto.id}/320/180",
                )
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadFollowedEntryIds(): Set<Int> {
        val raw = appMetaDao.getValue(KEY_FOLLOW_ENTRY_IDS) ?: return emptySet()
        return raw.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    suspend fun saveFollowedEntryIds(ids: Set<Int>) {
        val serialized = ids.sorted().joinToString(",")
        appMetaDao.upsert(AppMetaEntity(KEY_FOLLOW_ENTRY_IDS, serialized))
    }

    companion object {
        const val KEY_FOLLOW_ENTRY_IDS = "social_follow_entry_ids"
    }
}

data class SocialFeedEntry(
    val id: Int,
    val title: String,
    val anchorLine: String,
    val coverUrl: String,
)
