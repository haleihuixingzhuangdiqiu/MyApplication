package com.example.myapplication.game

import com.example.myapplication.database.HomePostCacheEntity
import com.example.myapplication.database.HomePostDao
import com.example.myapplication.network.api.JsonPlaceholderApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 演示：Retrofit 拉取列表 → Room 全量替换缓存 → UI 通过 Flow 观察。
 * （沿用 `home_posts` 表，后续 Ludex 迁移时可替换为游戏实体与 DAO。）
 */
@Singleton
class GameDemoRepository @Inject constructor(
    private val api: JsonPlaceholderApi,
    private val homePostDao: HomePostDao,
) {

    fun observeCachedPosts(): Flow<List<HomePostCacheEntity>> = homePostDao.observeAll()

    suspend fun refreshFromNetwork(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val remote = api.listPosts()
            val rows = remote.map { dto ->
                HomePostCacheEntity(
                    postId = dto.id,
                    title = dto.title,
                    body = dto.body,
                    coverImageUrl = "https://picsum.photos/seed/jp-${dto.id}/320/180",
                    fetchedAtMillis = System.currentTimeMillis(),
                )
            }
            homePostDao.replaceAll(rows)
            Result.success(rows.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
