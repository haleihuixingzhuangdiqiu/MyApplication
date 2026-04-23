package com.example.myapplication.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 业务 `ViewModel` 基类，把常见 UI 状态集中在一处，避免各业务各写一套 `MutableLiveData`/`Channel`。
 *
 * ## 三类对外状态（与职责）
 *
 * 1. **[loading]**：行内/局部「转圈」类加载态。适合列表刷新、按钮提交时在工具栏/卡片上显示，**不盖满整页**。
 *    与 [showPageLoading] 的**整页**遮罩是两套东西，可并行存在（一般不要同时用两种 loading 指同一次请求）。
 *
 * 2. **[userMessage]**：需要用户「撇一眼」的轻提示（如 Toast），走 [Event] 做**一次性消费**，避免转屏/重进重复弹。
 *    [showToast]、[postError] 都发到这里，**展示路径相同**，仅方法名帮助区分成功提示 vs 错误提示。
 *
 * 3. **[pageOverlay]**：全屏/整页级遮罩（转圈、空、错、可重试），与 [com.example.myapplication.framework.PageOverlayHost] 一一对应。
 *    需要「盖住列表、全屏说没数据/出错」时用这套；**恢复下方业务内容**请调用 [showPageContent]（对应状态 [PageOverlayState.Hidden]）。
 *
 * ## 与 UI 层如何接上线
 * `:core:framework` 里 `bindBaseViewModelUi` 会收集 [userMessage] 与 [pageOverlay]；若你未使用 [com.example.myapplication.framework.BaseBindingActivity]，
 * 需自己在 `Activity`/`Fragment` 里用 `collect` 相同逻辑。
 *
 * ## 协程 + Loading 的推荐写法
 * 对「自动 try/finally + 行内/整页 loading」见同模块的 [launch]、[launchInlineLoading]、[launchPageLoading]。
 */
abstract class BaseViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)

    /**
     * 行内/局部是否处于加载中。默认 `false`。
     * 与 [setLoading] 或 [VmLoadingStyle.INLINE] 的 [launch] 成对使用；**不会**自动驱动整页 [pageOverlay]。
     */
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _userMessage = MutableSharedFlow<Event<String>>(extraBufferCapacity = 64)

    /**
     * 轻提示文案（如 Toast 实现）。**所有** [showToast] / [postError] 都走此流，避免多通道同时弹两个 Toast。
     * 在 UI 层用 [Event.getContentIfNotHandled] 消费；若用框架绑定则已代劳。
     */
    val userMessage: SharedFlow<Event<String>> = _userMessage.asSharedFlow()

    private val _pageOverlay = MutableStateFlow<PageOverlayState>(PageOverlayState.Hidden)

    /**
     * 整页级遮罩状态。初始为 [PageOverlayState.Hidden]（不挡内容，与 [showPageContent] 一致）。
     * 子类用 [showPageLoading] / [showPageEmpty] / [showPageError] 切换；**收起遮罩、露出下方业务 UI** 用 [showPageContent]。
     */
    val pageOverlay: StateFlow<PageOverlayState> = _pageOverlay.asStateFlow()

    /**
     * 直接设置行内 [loading]；若使用 [VmLoadingStyle.INLINE] 的 [launch] 可不必手调，除非要中途改状态。
     */
    protected fun setLoading(show: Boolean) {
        _loading.update { show }
    }

    /** 供同模块 [launch] 在 `try` 里打开、`finally` 里关闭行内 loading。 */
    internal fun setLoadingInternal(show: Boolean) = setLoading(show)

    /**
     * 发一条轻提示（成功/提示类文案），经 [userMessage] 到 UI 层以 Toast 等方式展示。
     */
    protected fun showToast(msg: String) {
        viewModelScope.launch { _userMessage.emit(Event(msg)) }
    }

    /**
     * 发一条**错误/失败**向的轻提示；**实现上与 [showToast] 相同**（同一 [userMessage]），仅方法名表达语义。
     */
    protected fun postError(msg: String) = showToast(msg)

    /**
     * 将 [pageOverlay] 设为 [PageOverlayState.Loading]：全屏/整页转圈。用于首屏、整页数据加载。
     * 与 [loading] 的**局部**转圈是两套，勿混用同一次用户可感知的「唯一加载态」。
     */
    protected fun showPageLoading() {
        _pageOverlay.value = PageOverlayState.Loading
    }

    /** 供 [VmLoadingStyle.PAGE] 的 [launch] 在协程体执行前显示整页 Loading。 */
    internal fun showPageLoadingInternal() = showPageLoading()

    /**
     * 整页「空数据」态；[hint] 为 `null` 时用布局里默认空态文案。
     */
    protected fun showPageEmpty(hint: String? = null) {
        _pageOverlay.value = PageOverlayState.Empty(hint = hint)
    }

    /**
     * 整页错误态，可带重试。用户点重试时，优先走页面 [com.example.myapplication.framework.BaseBindingActivity.onPageOverlayRetry]，
     * 否则走 [onPageOverlayRetry]。
     */
    protected fun showPageError(message: String, allowRetry: Boolean = true) {
        _pageOverlay.value = PageOverlayState.Error(message, allowRetry)
    }

    /**
     * **收起**整页遮罩，**露出下方业务内容**（与「隐藏某块 UI」无冲突，指整页蒙层被移除）。
     * 对应 [PageOverlayState.Hidden]；与 [showPageLoading] 等成对使用。
     */
    protected fun showPageContent() {
        _pageOverlay.value = PageOverlayState.Hidden
    }

    /**
     * 整页错误蒙层上「重试」被点击，且页面上**没有**单独配置 `onPageOverlayRetry` 时回调。默认空实现，子类覆盖即可。
     */
    open fun onPageOverlayRetry() {}
}
