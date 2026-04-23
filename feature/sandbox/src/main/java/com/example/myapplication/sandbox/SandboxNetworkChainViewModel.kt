package com.example.myapplication.sandbox

import com.example.myapplication.mvvm.request.dataRequest
import com.example.myapplication.mvvm.request.envelopedRequest
import com.example.myapplication.mvvm.BaseViewModel
import com.example.myapplication.mvvm.launch
import com.example.myapplication.network.api.JsonPlaceholderApi
import com.example.myapplication.network.result.ApiResponse
import com.example.myapplication.network.result.ApiState
import com.example.myapplication.network.result.BusinessErrorCodes
import com.example.myapplication.network.result.asState
import com.example.myapplication.network.result.handleDataFlow
import com.example.myapplication.network.result.handleResultFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 网络链与 Flow 沙箱。下面每个方法的 **KDoc 第一行** 与试页面 `strings` 里按钮文字一致，只看本类即可对照 UI。
 * 对应布局：`@string/sandbox_net_btn_d_*` / `sandbox_net_btn_e_*` / `sandbox_net_btn_f_*`。
 */
@HiltViewModel
class SandboxNetworkChainViewModel @Inject constructor(
    private val api: JsonPlaceholderApi,
) : BaseViewModel() {

    private val _logLine = MutableStateFlow("点按钮试；真请求需联网。输出在上方。")
    val logLine: StateFlow<String> = _logLine.asStateFlow()

    // region 无壳 dataRequest

    /**
     * 浮层 + 真请求
     * （`@string/sandbox_net_btn_d_dialog`）
     */
    fun dataDialogSuccess() {
        dataRequest { api.listPosts() }
            .withDialogLoading()
            .onSuccess { posts ->
                val f = posts.firstOrNull()
                _logLine.value =
                    "[浮层+真请求] ${posts.size} 条, 首条 id=${f?.id} title=${f?.title?.take(20)}"
            }
            .start()
    }

    /**
     * 整页 + 真请求 + withAutoErrorPage
     * （`@string/sandbox_net_btn_d_page`；整页错重试见 [onPageOverlayRetry]）
     */
    fun dataPageWithOverlay() {
        dataRequest { api.listPosts() }
            .withPageLoading()
            .withAutoErrorPage(true)
            .onSuccess { posts ->
                _logLine.value = "[整页+真请求+自动整页错] ${posts.size} 条"
            }
            .start()
    }

    /**
     * deferred 浮层
     * （`@string/sandbox_net_btn_d_defer`；链内不自动开/关浮层，需外层已开或自行管）
     */
    fun dataDeferredDialog() {
        dataRequest { api.listPosts() }
            .withDialogLoadingDeferredInChain()
            .onSuccess { _logLine.value = "[deferred 浮层] ${it.size} 条" }
            .start()
    }

    /**
     * 仅 onNetwork
     * （`@string/sandbox_net_btn_d_net`）
     */
    fun dataNetworkOnNetworkOnly() {
        dataRequest { throw IOException("模拟断网/超时") }
            .withDialogLoading()
            .onSuccess { _logLine.value = "不应成功" }
            .onNetworkError { e -> _logLine.value = "[仅 onNetwork] ${e.userMessage}" }
            .start()
    }

    /**
     * 无 onNetwork：封装固定网错 Toast
     * （`@string/sandbox_net_btn_d_net_default`）
     */
    fun dataNetworkDefaultToastOnly() {
        dataRequest { throw IOException("模拟断网") }
            .withDialogLoading()
            .onSuccess { _logLine.value = "不应成功" }
            .start()
    }

    /**
     * dataRequest + onError（无 onNetwork）
     * （`@string/sandbox_net_btn_d_on_error`；未写 onNetwork 时网错进 onError，不弹封装固定 Toast）
     */
    fun dataRequestOnErrorOnly() {
        dataRequest { throw IOException("模拟断网") }
            .withDialogLoading()
            .onSuccess { _logLine.value = "不应成功" }
            .onError { f -> _logLine.value = "[dataRequest+onError] ${f.userMessage}" }
            .start()
    }

    // endregion

    // region 有壳 envelopedRequest

    /**
     * 封装码 -1 自动 message Toast
     * （`@string/sandbox_net_btn_e_def`）
     */
    fun envEncapsulationAutoMessage() {
        envelopedRequest { ApiResponse(-1, "库存不足", null) }
            .withDialogLoading()
            .onSuccess { _logLine.value = "不应成功" }
            .start()
    }

    /**
     * skipGlobal + onHubFirst（登出码）
     * （`@string/sandbox_net_btn_e_skip`）
     */
    fun envHubWithSkipGlobal() {
        envelopedRequest { ApiResponse(BusinessErrorCodes.UNAUTHORIZED_LOGOUT, "请重新登录", null) }
            .withDialogLoading()
            .skipGlobalBusinessDispatch()
            .onSuccess { _logLine.value = "不应成功" }
            .onHubFirstBusiness { f ->
                _logLine.value = "[skipGlobal + onHubFirst] ${f.userMessage}"
            }
            .start()
    }

    /**
     * 表外 code，仅 onError
     * （`@string/sandbox_net_btn_e_biz`）
     */
    fun envCustomOnError() {
        envelopedRequest<String?> { ApiResponse(2, "业务 2 号", null) }
            .withDialogLoading()
            .onSuccess { _logLine.value = "不应成功" }
            .onError { _logLine.value = "[onError 表外 code] ${it.userMessage}" }
            .start()
    }

    /**
     * skipAutoInfoToast + onError
     * （`@string/sandbox_net_btn_e_skip_enc`；封装码不自动发 info，走 onError）
     */
    fun envSkipEncapsulationOnError() {
        envelopedRequest { ApiResponse(-1, "库存不足(走 onError)", null) }
            .withDialogLoading()
            .skipAutoInfoToast()
            .onSuccess { _logLine.value = "不应成功" }
            .onError { _logLine.value = "[skip 封装码 + onError] ${it.userMessage}" }
            .start()
    }

    // endregion

    // region Flow

    /**
     * handleDataFlow 真请求
     * （`@string/sandbox_net_btn_f_data`）
     */
    fun flowHandleData() {
        launch {
            handleDataFlow { api.listPosts() }.collect { s ->
                _logLine.value = when (s) {
                    ApiState.Loading -> "[handleDataFlow] Loading"
                    is ApiState.Success -> "[handleDataFlow] ${s.data.size} 条"
                    is ApiState.Error -> "[handleDataFlow] ${s.userMessageOrDefault()}"
                }
            }
        }
    }

    /**
     * handleResultFlow 假包
     * （`@string/sandbox_net_btn_f_env`）
     */
    fun flowHandleResult() {
        launch {
            handleResultFlow { ApiResponse(0, null, "ok") }.collect { s ->
                _logLine.value = when (s) {
                    ApiState.Loading -> "[handleResultFlow] Loading"
                    is ApiState.Success -> "[handleResultFlow] ${s.data}"
                    is ApiState.Error -> "[handleResultFlow] ${s.userMessageOrDefault()}"
                }
            }
        }
    }

    /**
     * Flow.asState
     * （`@string/sandbox_net_btn_f_as`）
     */
    fun flowAsStateDemo() {
        launch {
            val states = flowOf(1, 2).asState().toList()
            _logLine.value = "[asState] $states"
        }
    }

    // endregion

    /**
     * 整页错误重试：与「整页 + 真请求 + withAutoErrorPage」同一路径 [dataPageWithOverlay]。
     */
    override fun onPageOverlayRetry() = dataPageWithOverlay()
}
