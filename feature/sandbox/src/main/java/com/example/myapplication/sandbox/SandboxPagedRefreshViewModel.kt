package com.example.myapplication.sandbox

import com.example.myapplication.mvvm.paging.BasePagedViewModel
import com.example.myapplication.mvvm.paging.PagedListConfig
import com.example.myapplication.mvvm.paging.PagedPage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * 沙箱：演示 [BasePagedViewModel] 最小实现。
 * 业务侧典型拆法：把本类中的 [pageConfig]、[loadPage] 换成真实接口与 DTO 映射；列表页只负责 [com.example.myapplication.mvvm.paging.RefreshListHost] + [RecyclerView.Adapter]。
 */
@HiltViewModel
class SandboxPagedRefreshViewModel @Inject constructor() : BasePagedViewModel<SandboxPagedItem>() {

    override val pageConfig = PagedListConfig(
        pageSize = 10,
        startPage = 1,
        enableRefresh = true,
        enableLoadMore = true,
    )

    /** 共 3 页数据，上拉第 3 页后 [hasMore] 为 false。 */
    private val maxPage = 3

    override suspend fun loadPage(isRefresh: Boolean, page: Int): PagedPage<SandboxPagedItem> {
        delay(450)
        if (page > maxPage) {
            return PagedPage(emptyList(), hasMore = false)
        }
        val startId = (page - 1) * pageConfig.pageSize + 1
        val items = List(pageConfig.pageSize) { i ->
            SandboxPagedItem(
                id = startId + i,
                title = "沙箱项 #${startId + i}（第 ${page} 页）",
            )
        }
        return PagedPage(
            items = items,
            hasMore = page < maxPage,
        )
    }
}

data class SandboxPagedItem(
    val id: Int,
    val title: String,
)
