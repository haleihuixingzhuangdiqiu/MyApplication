package com.example.myapplication.mvvm.paging

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.api.RefreshLayout
import kotlinx.coroutines.launch

/**
 * 对 [SmartRefreshLayout] + [RecyclerView] 的默认组装与 [ClassicsHeader] / [ClassicsFooter] 应用。
 * 与 [BasePagedViewModel] 绑定后，负责结束刷新/加载动画、无更多态，与业务 [RecyclerView.Adapter] 解耦。
 */
class RefreshListHost(
    private val refreshLayout: SmartRefreshLayout,
    val recyclerView: RecyclerView,
) {

    init {
        if (recyclerView.layoutManager == null) {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        }
    }

    /**
     * 使用经典头尾样式并应用 [config] 中的开关。请在 [bind] 之前调用（首屏前一次即可）。
     */
    fun installDefaults(pagedListConfig: PagedListConfig) {
        val ctx = refreshLayout.context
        refreshLayout.setRefreshHeader(ClassicsHeader(ctx))
        refreshLayout.setRefreshFooter(ClassicsFooter(ctx))
        refreshLayout.setEnableRefresh(pagedListConfig.enableRefresh)
        refreshLayout.setEnableLoadMore(pagedListConfig.enableLoadMore)
    }

    /**
     * 仅应用开关，不替换头/尾（自带头尾时可调用）。
     */
    fun applyConfig(config: PagedListConfig) {
        refreshLayout.setEnableRefresh(config.enableRefresh)
        refreshLayout.setEnableLoadMore(config.enableLoadMore)
    }

    /**
     * 将下拉/上拉与 [viewModel] 的 [BasePagedViewModel.runRefresh] / [BasePagedViewModel.runLoadMore] 对接，
     * 并监听 [noMore] 以同步「没有更多数据」态。
     */
    fun <ITEM> bind(
        viewModel: BasePagedViewModel<ITEM>,
        owner: LifecycleOwner,
    ) {
        refreshLayout.setOnRefreshListener { layout: RefreshLayout ->
            owner.lifecycleScope.launch {
                val ok = viewModel.runRefresh()
                layout.finishRefresh(ok)
            }
        }
        refreshLayout.setOnLoadMoreListener { layout: RefreshLayout ->
            owner.lifecycleScope.launch {
                if (viewModel.items.value.isEmpty()) {
                    layout.finishLoadMore(false)
                    return@launch
                }
                val ok = viewModel.runLoadMore()
                layout.finishLoadMore(ok)
            }
        }
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.noMore.collect { noMore ->
                        refreshLayout.setNoMoreData(noMore)
                    }
                }
            }
        }
    }
}
