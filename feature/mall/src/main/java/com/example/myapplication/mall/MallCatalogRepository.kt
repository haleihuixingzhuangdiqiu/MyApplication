package com.example.myapplication.mall

import com.example.myapplication.database.AppMetaDao
import com.example.myapplication.database.AppMetaEntity
import com.example.myapplication.network.api.JsonPlaceholderApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** 演示商品目录：网络拉取（不入库，避免与 `game` 的 `home_post_cache` 互相覆盖）。购物车 ID 集持久化到 `app_meta`。 */
@Singleton
class MallCatalogRepository @Inject constructor(
    private val api: JsonPlaceholderApi,
    private val appMetaDao: AppMetaDao,
) {

    suspend fun fetchCatalog(): Result<List<MallItem>> = withContext(Dispatchers.IO) {
        try {
            val remote = api.listPosts()
            val rows = remote.map { dto ->
                MallItem(
                    id = dto.id,
                    title = dto.title.trim(),
                    bodyPreview = dto.body.replace("\n", " ").take(120),
                    coverUrl = "https://picsum.photos/seed/mall-${dto.id}/320/320",
                    priceLabel = "¥${dto.id % 99 + 1}.00",
                )
            }
            Result.success(rows)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loadCartPostIds(): Set<Int> {
        val raw = appMetaDao.getValue(KEY_CART_POST_IDS) ?: return emptySet()
        return raw.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
    }

    suspend fun saveCartPostIds(ids: Set<Int>) {
        val serialized = ids.sorted().joinToString(",")
        appMetaDao.upsert(AppMetaEntity(KEY_CART_POST_IDS, serialized))
    }

    companion object {
        const val KEY_CART_POST_IDS = "mall_cart_post_ids"
    }
}

data class MallItem(
    val id: Int,
    val title: String,
    val bodyPreview: String,
    val coverUrl: String,
    val priceLabel: String,
)
