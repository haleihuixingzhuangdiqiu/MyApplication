package com.example.myapplication.mvvm.paging

import com.example.myapplication.mvvm.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * 与 [RefreshListHost] 配合的通用分页/刷新逻辑。
 * 子类只实现 [loadPage] 与 [pageConfig]；在 UI 层通过 [runRefresh] / [runLoadMore] 驱动 SmartRefresh 结束态。
 */
abstract class BasePagedViewModel<ITEM> : BaseViewModel() {

    abstract val pageConfig: PagedListConfig

    /**
     * 拉取一页。下拉刷新、首次进入时 [isRefresh] 为 `true`；上拉为 `false`。
     * [page] 为业务页码，从 [PagedListConfig.startPage] 起递增，由本基类维护。
     */
    protected abstract suspend fun loadPage(isRefresh: Boolean, page: Int): PagedPage<ITEM>

    private val _items = MutableStateFlow<List<ITEM>>(emptyList())
    val items: StateFlow<List<ITEM>> = _items.asStateFlow()

    private val _noMore = MutableStateFlow(false)
    val noMore: StateFlow<Boolean> = _noMore.asStateFlow()

    private val _loadingRefresh = MutableStateFlow(false)
    val loadingRefresh: StateFlow<Boolean> = _loadingRefresh.asStateFlow()

    private val _loadingMore = MutableStateFlow(false)
    val loadingMore: StateFlow<Boolean> = _loadingMore.asStateFlow()

    private var nextLoadPage: Int = 0

    /** 供界面首进或重试时调用，等价于一次全量刷新。 */
    suspend fun runRefresh(): Boolean {
        if (_loadingRefresh.value) return false
        _loadingRefresh.value = true
        return try {
            val page = withContext(Dispatchers.IO) {
                loadPage(isRefresh = true, page = pageConfig.startPage)
            }
            _items.value = page.items
            nextLoadPage = pageConfig.startPage + 1
            _noMore.value = !page.hasMore
            true
        } catch (e: Exception) {
            postError(e.message ?: "刷新失败")
            false
        } finally {
            _loadingRefresh.value = false
        }
    }

    /** 由 SmartRefresh 上拉回调触发，勿与 [runRefresh] 并发。 */
    suspend fun runLoadMore(): Boolean {
        if (!pageConfig.enableLoadMore || _noMore.value || _loadingMore.value || _loadingRefresh.value) {
            return false
        }
        if (_items.value.isEmpty()) return false
        _loadingMore.value = true
        return try {
            val p = nextLoadPage
            val page = withContext(Dispatchers.IO) {
                loadPage(isRefresh = false, page = p)
            }
            _items.value += page.items
            if (page.hasMore) {
                nextLoadPage = p + 1
            } else {
                _noMore.value = true
            }
            true
        } catch (e: Exception) {
            postError(e.message ?: "加载更多失败")
            false
        } finally {
            _loadingMore.value = false
        }
    }
}
