package com.example.myapplication.mvvm.paging

/**
 * 通用列表分页/刷新能力开关与分页参数。交给 [BasePagedViewModel] 只读使用。
 */
data class PagedListConfig(
    /** 每页条数。 */
    val pageSize: Int = 20,
    /**
     * 与 [BasePagedViewModel.loadPage] 的 [page] 参数对齐：首刷、下拉刷新时请求的页码。
     * 常见为 `0` 或 `1`，视后端约定。
     */
    val startPage: Int = 1,
    /** 是否允许下拉刷新（会同步到 [RefreshListHost] 的 [enableRefresh]）。 */
    val enableRefresh: Boolean = true,
    /** 是否允许上拉加载。 */
    val enableLoadMore: Boolean = true,
)
