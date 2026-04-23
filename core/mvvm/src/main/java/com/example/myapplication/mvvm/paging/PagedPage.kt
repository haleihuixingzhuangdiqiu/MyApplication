package com.example.myapplication.mvvm.paging

/**
 * 单页拉取结果；[hasMore] 为 `false` 时上拉会进入「没有更多」并触发 SmartRefresh 无更多态。
 */
data class PagedPage<out T>(
    val items: List<T>,
    val hasMore: Boolean,
)
