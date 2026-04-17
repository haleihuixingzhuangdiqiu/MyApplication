package com.example.myapplication.framework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 业务 ViewModel 基类：Loading、[SharedFlow] 单次错误事件、[Channel] Toast 与 [StateFlow] 消息。
 */
abstract class BaseViewModel : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _uiEvents = Channel<UiEvent>(Channel.BUFFERED)
    val uiEvents: Flow<UiEvent> = _uiEvents.receiveAsFlow()

    private val _errorMessage = MutableSharedFlow<Event<String>>(extraBufferCapacity = 64)
    val errorMessage: SharedFlow<Event<String>> = _errorMessage.asSharedFlow()

    private val _messageFlow = MutableStateFlow<String?>(null)
    val messageFlow: StateFlow<String?> = _messageFlow.asStateFlow()

    private val _pageOverlay = MutableStateFlow<PageOverlayState>(PageOverlayState.Hidden)
    val pageOverlay: StateFlow<PageOverlayState> = _pageOverlay.asStateFlow()

    protected fun setLoading(show: Boolean) {
        _loading.update { show }
    }

    /** 全屏遮罩：加载中（与 [loading] 独立，用于首屏/整页状态）。 */
    protected fun showPageLoading() {
        _pageOverlay.value = PageOverlayState.Loading
    }

    /** 全屏遮罩：空数据；[hint] 为空或全空白时用默认「暂无数据」。 */
    protected fun showPageEmpty(hint: String? = null) {
        _pageOverlay.value = PageOverlayState.Empty(hint = hint)
    }

    /** 全屏遮罩：错误；默认显示重试按钮，[allowRetry] 为 false 时仅展示文案。 */
    protected fun showPageError(message: String, allowRetry: Boolean = true) {
        _pageOverlay.value = PageOverlayState.Error(message, allowRetry)
    }

    protected fun hidePageOverlay() {
        _pageOverlay.value = PageOverlayState.Hidden
    }

    /** 用户点击错误遮罩上的「重试」时由框架调用（仅当页面未设置 Activity/Fragment 的 onPageOverlayRetry 回调时）。 */
    open fun onPageOverlayRetry() {}

    protected fun showToast(msg: String) {
        viewModelScope.launch { _uiEvents.send(UiEvent.Toast(msg)) }
    }

    protected fun postError(msg: String) {
        viewModelScope.launch { _errorMessage.emit(Event(msg)) }
    }

    protected fun emitMessage(msg: String?) {
        _messageFlow.value = msg
    }
}
