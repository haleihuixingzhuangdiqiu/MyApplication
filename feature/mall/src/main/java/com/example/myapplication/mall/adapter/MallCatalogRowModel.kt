package com.example.myapplication.mall.adapter

import androidx.annotation.Keep
import com.example.myapplication.mall.MallItem

/** ModuleAdapter 行模型：目录项 + 购物车状态（演示数据）。 */
@Keep
data class MallCatalogRowModel(
    val postId: Int,
    val title: String,
    val subtitle: String,
    val priceLabel: String,
    val coverUrl: String,
    val inCart: Boolean,
) {
    companion object {
        fun from(item: MallItem, inCart: Boolean): MallCatalogRowModel =
            MallCatalogRowModel(
                postId = item.id,
                title = item.title,
                subtitle = item.bodyPreview,
                priceLabel = item.priceLabel,
                coverUrl = item.coverUrl,
                inCart = inCart,
            )
    }
}
