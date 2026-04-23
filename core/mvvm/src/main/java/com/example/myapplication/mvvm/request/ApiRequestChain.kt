package com.example.myapplication.mvvm.request

import androidx.lifecycle.viewModelScope
import com.example.myapplication.mvvm.BaseViewModel
import com.example.myapplication.network.result.ApiResponse
import com.example.myapplication.network.result.ApiResult
import com.example.myapplication.network.result.RequestChainErrorDispatch
import com.example.myapplication.network.result.failureMessageOrNull
import com.example.myapplication.network.result.handleData as runHandleData
import com.example.myapplication.network.result.handleResult as runHandleEnvelopedResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** 链式请求失败时，统一给 UI 展示的一条 [userMessage]；区分业务与网络，原始结果在 [result]。 */
sealed class ApiRequestFailure {
    /** 可简短展示给用户的说明（多来自服务端 message 或网络映射）。 */
    abstract val userMessage: String

    /** 有壳 [ApiResult.BusinessError] 时的包装。 */
    data class Business(override val userMessage: String, val result: ApiResult.BusinessError) : ApiRequestFailure()
    /** 无壳 / 有壳里网络失败时的包装。 */
    data class Network(override val userMessage: String, val result: ApiResult.NetworkError) : ApiRequestFailure()
}

/**
 * 无 [ApiResponse] 包壳的链：协程体直接返回 [T]（或内部抛/映射为网络错）。
 * 仅会产生 [ApiResult.NetworkError]；未拦截时 [RequestChainErrorDispatch.USER_VISIBLE_NETWORK_ERROR] 作 Toast，[withAutoErrorPage] 仅在整页 loading 时影响整页错。
 */
class DataRequestChain<T> internal constructor(
    private val vm: BaseViewModel,
    private val block: suspend () -> T,
) {
    private var load = LoadKind.None
    private var chainShowsInitialLoading: Boolean = true
    private var autoShowErrorPage: Boolean = false
    private var onSuccessBlock: ((T) -> Unit)? = null
    private var onNetworkErrorBlock: ((ApiRequestFailure.Network) -> Unit)? = null
    private var onErrorBlock: ((ApiRequestFailure) -> Unit)? = null

    /**
     * 使用 [BaseViewModel.dialogLoading] 浮层；在 [start] 开头 `true`、[finally] 里 `false`（成对开关）。
     */
    fun withDialogLoading() = apply {
        load = LoadKind.Float
        chainShowsInitialLoading = true
    }

    /**
     * 仍选用浮层语义，但**不**在链内自动开/关 [dialogLoading]；用于外层已开浮层、多步共用一个浮层等场景。见 [withDialogLoading] 对照。
     */
    fun withDialogLoadingDeferredInChain() = apply {
        load = LoadKind.Float
        chainShowsInitialLoading = false
    }

    /**
     * 使用 [BaseViewModel] 整页 [pageOverlay]：开始时 [showPageLoadingForRequestChain]、成功时 [showPageContentForRequestChain]。
     */
    fun withPageLoading() = apply {
        load = LoadKind.Page
        chainShowsInitialLoading = true
    }

    /**
     * 整页 loading，但**不**在 [start] 里自动开/关整页态；由页面先 [showPageLoading] 等，链不抢生命周期。
     */
    fun withPageLoadingDeferredInChain() = apply {
        load = LoadKind.Page
        chainShowsInitialLoading = false
    }

    /**
     * 是否自动出整页错（`showPageError`），默认关。只影响整页态，不决定 Toast；Toast 看 `onNetwork`/`onError` 与默认固定文案。
     */
    fun withAutoErrorPage(enabled: Boolean = false) = apply { autoShowErrorPage = enabled }

    /**
     * 成功时回调，**必调**，否则 [start] 会报错。成功且为整页 loading 时会先 [showPageContentForRequestChain] 再进本块。
     */
    fun onSuccess(block: (T) -> Unit) = apply { onSuccessBlock = block }

    /**
     * 仅网络失败时回调；有则**只**进这里。Toast 在 block 内自行 `postError` / `showToast` 等。未设则看 [onError]；都未设则走封装固定短 Toast。
     */
    fun onNetworkError(block: (ApiRequestFailure.Network) -> Unit) = apply { onNetworkErrorBlock = block }

    /**
     * 在 [onNetworkError] 未设时，网络失败会进本回调（若本回调存在）。有则**只**进这里。仍用于统一兜底网络类失败。
     */
    fun onError(block: (ApiRequestFailure) -> Unit) = apply { onErrorBlock = block }

    /**
     * 在 [viewModelScope] 中启动，执行 `dataRequest { … }` 内的挂起体；须已 [onSuccess]。
     */
    fun start(): Job {
        val onOk = onSuccessBlock ?: error("DataRequestChain: 需先 onSuccess { } 再 start()")
        return vm.viewModelScope.launch {
            if (load == LoadKind.Float && chainShowsInitialLoading) vm.setDialogLoadingForRequestChain(true)
            if (load == LoadKind.Page && chainShowsInitialLoading) vm.showPageLoadingForRequestChain()
            try {
                when (val r = runHandleData { block() }) {
                    is ApiResult.Success -> {
                        if (load == LoadKind.Page) vm.showPageContentForRequestChain()
                        onOk(r.data)
                    }
                    else -> deliverNetworkOnlyFailure(
                        r,
                        onNetwork = onNetworkErrorBlock,
                        onAny = onErrorBlock,
                        autoShowErrorPage = autoShowErrorPage,
                        load = load,
                        vm = vm,
                    )
                }
            } finally {
                if (load == LoadKind.Float && chainShowsInitialLoading) vm.setDialogLoadingForRequestChain(false)
            }
        }
    }
}

/**
 * 有 [ApiResponse] 包壳的链。业务/网络/特码/封装码 分派与 [RequestChainErrorDispatch] 及各 `onXxx` 一一对应，详见方法注释。
 */
class EnvelopedRequestChain<T> internal constructor(
    private val vm: BaseViewModel,
    private val block: suspend () -> ApiResponse<T>,
) {
    private var load = LoadKind.None
    private var chainShowsInitialLoading: Boolean = true
    private var autoShowErrorPage: Boolean = false
    /** 为 `true` 时，对 [RequestChainErrorDispatch.encapsulationMessageToastCodes] 不自动用 message 发 info。 */
    private var noAutoInfoToast: Boolean = false
    private var skipGlobalDispatch: Boolean = false
    private var onSuccessBlock: ((T) -> Unit)? = null
    private var onHubFirstBusinessBlock: ((ApiRequestFailure.Business) -> Unit)? = null
    private var onNetworkErrorBlock: ((ApiRequestFailure.Network) -> Unit)? = null
    private var onErrorBlock: ((ApiRequestFailure) -> Unit)? = null

    /**
     * 同 [DataRequestChain.withDialogLoading]：链内对 [BaseViewModel.dialogLoading] 成对开关。
     */
    fun withDialogLoading() = apply {
        load = LoadKind.Float
        chainShowsInitialLoading = true
    }

    /**
     * 同 [DataRequestChain.withDialogLoadingDeferredInChain]：不自动开/关浮层。
     */
    fun withDialogLoadingDeferredInChain() = apply {
        load = LoadKind.Float
        chainShowsInitialLoading = false
    }

    /**
     * 同 [DataRequestChain.withPageLoading]：整页 loading/成功/收起。
     */
    fun withPageLoading() = apply {
        load = LoadKind.Page
        chainShowsInitialLoading = true
    }

    /**
     * 同 [DataRequestChain.withPageLoadingDeferredInChain]：不自动开/关整页态。
     */
    fun withPageLoadingDeferredInChain() = apply {
        load = LoadKind.Page
        chainShowsInitialLoading = false
    }

    /**
     * 失败时是否对 [BaseViewModel.pageOverlay] 自动 [BaseViewModel.showPageErrorForRequestChain]（仅**整页** loading 时生效），默认关。**不**管业务/网络 Toast，Toast 走各 [onXxx] 与 [RequestChainErrorDispatch] 默认分支。
     */
    fun withAutoErrorPage(enabled: Boolean = false) = apply { autoShowErrorPage = enabled }

    /**
     * 封装码表内（见 [RequestChainErrorDispatch.encapsulationMessageToastCodes]）是否**不要**链自动用 `message` 发 **info** Toast。
     * - 默认无调用 = 会按码表自动发；`.skipAutoInfoToast()` 或 `true` = 不发，用 [onError] 自管；`false` 恢复链自动发。
     */
    fun skipAutoInfoToast(yes: Boolean = true) = apply { noAutoInfoToast = yes }

    /**
     * 为 `true` 时关闭有壳解包侧对**全局**业务派发的默认行为（`dispatchGlobalBusiness = false`），特码/业务在链上 [onHubFirstBusiness] / [onError] 自管；`false` 时仍经 :core:network 已接的 Hub/单点（若有）。
     */
    fun skipGlobalBusinessDispatch() = apply { skipGlobalDispatch = true }

    /**
     * 成功时回调，**必调**；[ApiResponse] 为成功时取 `data` 传入。整页态成功前会先 [showPageContentForRequestChain]。
     */
    fun onSuccess(block: (T) -> Unit) = apply { onSuccessBlock = block }

    /**
     * 仅对 [RequestChainErrorDispatch.hubFirstBusinessCodes] 中业务特码。写了则**只**进这里，不调 [onError]、不 `postError`；不写则交全局/Hub 侧，链内不再向下分派。可与 [skipGlobalBusinessDispatch] 配合。
     */
    fun onHubFirstBusiness(block: (ApiRequestFailure.Business) -> Unit) = apply { onHubFirstBusinessBlock = block }

    /**
     * 有壳链中的**网络**失败；有则**只**进这里。Toast 在 block 内自管。未设时网络错再试 [onError]；都未设则固定短网错 Toast 与（可选）整页错。
     */
    fun onNetworkError(block: (ApiRequestFailure.Network) -> Unit) = apply { onNetworkErrorBlock = block }

    /**
     * **非** hub 特码时的业务失败、以及**未**写 [onNetworkError] 时的网络失败。有则**只**进这里。hub 特码**不会**进入本回调。
     */
    fun onError(block: (ApiRequestFailure) -> Unit) = apply { onErrorBlock = block }

    /**
     * 与 [DataRequestChain.start] 相同：启动后执行 [block] 取 [ApiResponse]。须已 [onSuccess]。
     */
    fun start(): Job {
        val onOk = onSuccessBlock ?: error("EnvelopedRequestChain: 需先 onSuccess { } 再 start()")
        return vm.viewModelScope.launch {
            if (load == LoadKind.Float && chainShowsInitialLoading) vm.setDialogLoadingForRequestChain(true)
            if (load == LoadKind.Page && chainShowsInitialLoading) vm.showPageLoadingForRequestChain()
            try {
                when (
                    val r = runHandleEnvelopedResult(
                        dispatchGlobalBusiness = !skipGlobalDispatch,
                        call = block,
                    )
                ) {
                    is ApiResult.Success -> {
                        if (load == LoadKind.Page) vm.showPageContentForRequestChain()
                        onOk(r.data)
                    }
                    else -> deliverEnvelopedFailure(
                        r = r,
                        onHubFirst = onHubFirstBusinessBlock,
                        onNetwork = onNetworkErrorBlock,
                        onAny = onErrorBlock,
                        autoShowErrorPage = autoShowErrorPage,
                        noAutoInfoToast = noAutoInfoToast,
                        load = load,
                        vm = vm,
                    )
                }
            } finally {
                if (load == LoadKind.Float && chainShowsInitialLoading) vm.setDialogLoadingForRequestChain(false)
            }
        }
    }
}

private enum class LoadKind { None, Float, Page }

private fun <T> deliverNetworkOnlyFailure(
    r: ApiResult<T>,
    onNetwork: ((ApiRequestFailure.Network) -> Unit)?,
    onAny: ((ApiRequestFailure) -> Unit)?,
    autoShowErrorPage: Boolean,
    load: LoadKind,
    vm: BaseViewModel,
) {
    if (r is ApiResult.Success) return
    if (r !is ApiResult.NetworkError) return
    val f = ApiRequestFailure.Network(r.userMessageOrDefault(), r)
    if (onNetwork != null) {
        onNetwork(f)
        return
    }
    if (onAny != null) {
        onAny(f)
        return
    }
    val netMsg = RequestChainErrorDispatch.USER_VISIBLE_NETWORK_ERROR
    vm.postErrorForRequestChainDefault(netMsg)
    if (autoShowErrorPage && load == LoadKind.Page) {
        vm.showPageErrorForRequestChain(netMsg)
    }
}

private fun <T> deliverEnvelopedFailure(
    r: ApiResult<T>,
    onHubFirst: ((ApiRequestFailure.Business) -> Unit)?,
    onNetwork: ((ApiRequestFailure.Network) -> Unit)?,
    onAny: ((ApiRequestFailure) -> Unit)?,
    autoShowErrorPage: Boolean,
    noAutoInfoToast: Boolean,
    load: LoadKind,
    vm: BaseViewModel,
) {
    val f: ApiRequestFailure = when (r) {
        is ApiResult.BusinessError -> {
            val msg = (r as ApiResult<*>).failureMessageOrNull()!!
            ApiRequestFailure.Business(msg, r)
        }
        is ApiResult.NetworkError -> ApiRequestFailure.Network(r.userMessageOrDefault(), r)
        is ApiResult.Success -> return
    }
    when (f) {
        is ApiRequestFailure.Business -> {
            val code = f.result.code
            if (RequestChainErrorDispatch.isHubFirstBusinessCode(code)) {
                if (onHubFirst != null) {
                    onHubFirst(f)
                }
                return
            }
            if (onAny != null) {
                onAny(f)
                return
            }
            if (
                RequestChainErrorDispatch.isEncapsulationMessageToastCode(code) &&
                !noAutoInfoToast
            ) {
                vm.postInfoForRequestChainDefault(f.userMessage)
                if (autoShowErrorPage && load == LoadKind.Page) {
                    vm.showPageErrorForRequestChain(f.userMessage)
                }
            }
        }
        is ApiRequestFailure.Network -> {
            if (onNetwork != null) {
                onNetwork(f)
                return
            }
            if (onAny != null) {
                onAny(f)
                return
            }
            val netMsg = RequestChainErrorDispatch.USER_VISIBLE_NETWORK_ERROR
            vm.postErrorForRequestChainDefault(netMsg)
            if (autoShowErrorPage && load == LoadKind.Page) {
                vm.showPageErrorForRequestChain(netMsg)
            }
        }
    }
}

/**
 * 无 [ApiResponse] 包壳的链式入口。在 [block] 中直接 `return` 数据或经 `handleData` 映射；再 `.withXxx().onSuccess { }.start()`。
 * 链类型 [DataRequestChain]。
 */
fun <T> BaseViewModel.dataRequest(block: suspend () -> T): DataRequestChain<T> =
    DataRequestChain(this, block)

/**
 * 有 [ApiResponse] 包壳的链式入口。在 [block] 中 `return` [ApiResponse]；再组 loading / 各错误回调后 [EnvelopedRequestChain.start]。
 * 业务码分派与 [RequestChainErrorDispatch] 及 `onHubFirstBusiness` / [EnvelopedRequestChain.onError] 等配合使用。
 */
fun <T> BaseViewModel.envelopedRequest(block: suspend () -> ApiResponse<T>): EnvelopedRequestChain<T> =
    EnvelopedRequestChain(this, block)
