package com.example.myapplication.sandbox

import com.example.myapplication.mvvm.BaseViewModel
import com.example.myapplication.mvvm.launch
import com.example.myapplication.mvvm.launchInlineLoading
import com.example.myapplication.mvvm.launchPageLoading
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SandboxViewModelTestViewModel : BaseViewModel() {

    private val _status = MutableStateFlow("等待操作")
    val status: StateFlow<String> = _status.asStateFlow()

    fun triggerToast() {
        _status.value = "调用 showToast()"
        showToast("showToast() 已触发")
    }

    fun triggerError() {
        _status.value = "调用 postError()"
        postError("postError() 已触发")
    }

    fun triggerMessage() {
        _status.value = "调用 showToast()（原 emitMessage 已并入 userMessage）"
        showToast("showToast() / userMessage 已触发")
    }

    fun triggerInlineLoadingSuccess() {
        launchInlineLoading {
            _status.value = "launchInlineLoading 成功中..."
            delay(900)
            _status.value = "launchInlineLoading 成功结束"
            showToast("Inline loading 完成")
        }
    }

    fun triggerInlineLoadingFailure() {
        launchInlineLoading(
            catchBlock = { e ->
                _status.value = "launchInlineLoading 失败: ${e.message}"
                postError("Inline loading 失败: ${e.message}")
            },
        ) {
            _status.value = "launchInlineLoading 失败中..."
            delay(900)
            error("模拟异常")
        }
    }

    fun triggerPageLoadingThenHide() {
        _status.value = "showPageLoading() -> showPageContent()"
        showPageLoading()
        launch {
            delay(900)
            showPageContent()
            _status.value = "页面遮罩已隐藏"
        }
    }

    fun triggerPageEmpty() {
        _status.value = "调用 showPageEmpty()"
        showPageEmpty("这是 showPageEmpty() 的自定义文案")
    }

    fun triggerPageError() {
        _status.value = "调用 showPageError()"
        showPageError("这是 showPageError() 的测试文案", allowRetry = true)
    }

    fun triggerPageLoadingToSuccess() {
        launchPageLoading {
            _status.value = "launchPageLoading -> showPageContent() 执行中..."
            delay(900)
            showPageContent()
            _status.value = "launchPageLoading -> showPageContent() 已完成"
        }
    }

    fun triggerPageLoadingToEmpty() {
        launchPageLoading {
            _status.value = "launchPageLoading -> Empty 执行中..."
            delay(900)
            showPageEmpty("异步完成后切到了 Empty")
            _status.value = "launchPageLoading -> Empty 已完成"
        }
    }

    fun triggerPageLoadingToError() {
        launchPageLoading {
            _status.value = "launchPageLoading -> Error 执行中..."
            delay(900)
            showPageError("异步完成后切到了 Error", allowRetry = true)
            _status.value = "launchPageLoading -> Error 已完成"
        }
    }

    override fun onPageOverlayRetry() {
        _status.value = "触发 onPageOverlayRetry()"
        showPageContent()
        showToast("onPageOverlayRetry() 已触发")
    }
}
