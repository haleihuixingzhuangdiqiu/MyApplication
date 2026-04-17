package com.example.myapplication.game.adapter

import androidx.annotation.Keep

/** ViewPager2 顶部运营位（与 Feed 列表数据源分离）。 */
@Keep
data class GameBannerUi(
    val id: Int,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
)
