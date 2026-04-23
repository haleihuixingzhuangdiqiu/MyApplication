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
 * 业务 `ViewModel` 基类：行内/浮层 [loading]/[dialogLoading]、轻提示 [userMessage]（[UiUserMessage]：网络类 [postError] → 带叉；业务/一般 [postInfo] / [showToast] → info）、整页 [pageOverlay]。
 * 网络请求可优先用 [com.example.myapplication.mvvm.request.dataRequest] / [com.example.myapplication.mvvm.request.envelopedRequest]；协程+loading 见 [launch] 系列。UI 收集见 `bindBaseViewModelUi` 或自写 `collect`。
 */
abstract class BaseViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)

    /**
     * 行内/局部是否处于加载中。默认 `false`。
     * 与 [setLoading] 或 [VmLoadingStyle.INLINE] 的 [launch] 成对使用；**不会**自动驱动整页 [pageOverlay]。
     */
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _dialogLoading = MutableStateFlow(false)

    /** 全屏/窗口级转圈，链式 `withDialogLoading` 会开关；在页面里 collect 后接 Dialog。 */
    val dialogLoading: StateFlow<Boolean> = _dialogLoading.asStateFlow()

    private val _userMessage = MutableSharedFlow<Event<UiUserMessage>>(extraBufferCapacity = 64)

    /**
     * 轻提示（如 Toast）。[showToast] / [postInfo] 为 [UiUserMessage.InfoMessage]；[postError] 为 [UiUserMessage.ErrorMessage]（带叉 error 样式）。
     * 在 UI 层用 [Event.getContentIfNotHandled] 消费；若用框架绑定则已代劳。
     */
    val userMessage: SharedFlow<Event<UiUserMessage>> = _userMessage.asSharedFlow()

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

    protected fun setDialogLoading(show: Boolean) {
        _dialogLoading.value = show
    }

    /** 供链式请求开关浮层 loading。 */
    fun setDialogLoadingForRequestChain(show: Boolean) = setDialogLoading(show)

    /**
     * 发一条**业务/一般**向轻提示，UI 为 info 样式（[UiUserMessage.InfoMessage]）。
     */
    protected fun showToast(msg: String) {
        viewModelScope.launch { _userMessage.emit(Event(UiUserMessage.InfoMessage(msg))) }
    }

    /**
     * 同 [showToast]：info 样式，语义上强调「业务提示」时可显式调用。
     */
    protected fun postInfo(msg: String) = showToast(msg)

    /**
     * 发一条**网络/系统**向失败提示，UI 为带叉的 error 样式（[UiUserMessage.ErrorMessage] / [com.example.myapplication.common.toast.showError]）。
     */
    protected fun postError(msg: String) {
        viewModelScope.launch { _userMessage.emit(Event(UiUserMessage.ErrorMessage(msg))) }
    }

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
     * 整页错误态，可带重试。用户点重试时，优先走页面 [BaseBindingActivity.onPageOverlayRetry]，
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

    fun showPageContentForRequestChain() = showPageContent()
    fun showPageLoadingForRequestChain() = showPageLoading()
    fun showPageErrorForRequestChain(message: String) = showPageError(message, true)

    /** 链式里**网络**类默认轻提示，error 样式（带叉）。 */
    fun postErrorForRequestChainDefault(msg: String) = postError(msg)

    /** 链式里**业务** `message` 轻提示，info 样式。 */
    fun postInfoForRequestChainDefault(msg: String) = postInfo(msg)
}
