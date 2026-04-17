package com.example.myapplication.game.adapter

import androidx.annotation.Keep

/** 全宽分区标题（ModuleAdapter 非 grid 注册，占满 span）。 */
@Keep
data class GameSectionTitleModel(
    val sectionId: String,
    val title: String,
    val subtitle: String?,
)
