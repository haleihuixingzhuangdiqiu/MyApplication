package com.example.myapplication.mvvm

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * 与 [BaseViewModel] 配合使用的**协程启动方式**与**加载样式**枚举。
 *
 * ## 为什么不用直接 `viewModelScope.launch { }`？
 * 业务里大量重复：`try { 开 loading → 请求 } catch { 提示 } finally { 关 loading }`。
 * 本文件用 [VmLoadingStyle] 把「开/关哪一种 loading」固化在 [launch] 里，减少漏关、关错（例如把整页遮罩在 `finally` 里误关）的问题。
 *
 * ## 三种 [VmLoadingStyle] 怎么选？
 * - **[NONE]**：完全不管 [BaseViewModel] 的加载态，等同于手写 `viewModelScope.launch`，适合已自己管好的逻辑。
 * - **[INLINE]**：自动操作 [BaseViewModel.loading]（行内/局部转圈）。**仅**在协程 `finally` 里关行内 loading；**不会**动 [BaseViewModel.pageOverlay]。
 * - **[PAGE]**：在协程**开始**时调 [BaseViewModel.showPageLoading]（整页蒙层）。**不会**在 `finally` 里自动关掉整页蒙层——你必须在 `tryBlock` 里根据结果调用
 *   [BaseViewModel.showPageContent] / [BaseViewModel.showPageEmpty] / [BaseViewModel.showPageError]，否则用户会一直看到转圈。
 *   这样设计是为了避免「业务已切到 Error/Empty，`finally` 又一把打成 [PageOverlayState.Hidden]」的竞态。
 *
 * 三种行为单测见 `core/mvvm/src/test/.../VmLoadingStyleLaunchTest.kt`。
 */
enum class VmLoadingStyle {
    /**
     * 不自动改 [BaseViewModel.loading] 与 [BaseViewModel.pageOverlay]。
     * 适合：纯导航、埋点、或与 UI 加载态无关的后台任务。
     */
    NONE,

    /**
     * 进入 `try` 时将 [BaseViewModel.loading] 对应状态置为展示，在 `finally` 中自动关行内 loading。
     * **用于**：下拉刷新、按钮提交时在**不盖满整页**的区域显示转圈。
     */
    INLINE,

    /**
     * 进入 `try` 前将 [BaseViewModel.pageOverlay] 设为 [PageOverlayState.Loading]（整页转圈）。
     * **不会**在 `finally` 里调用 [BaseViewModel.showPageContent]——结束后你必须在 [tryBlock] 里显式切到
     * [BaseViewModel.showPageContent] / [showPageEmpty] / [showPageError] 之一。
     */
    PAGE,
}

/**
 * 在 [viewModelScope] 上启动协程，并按 [loadingStyle] 自动处理 [BaseViewModel] 的加载态。
 *
 * @param loadingStyle 见 [VmLoadingStyle]；默认 [VmLoadingStyle.NONE]。
 * @param catchBlock 非 [CancellationException] 的异常会进入此块（例如网络失败）；默认吞掉不做任何事，**请自行** [postError] 等。
 * @param finallyBlock 无论成功失败都会在 `try/catch` 之后执行（协程取消时也会走；注意与 [loadingStyle] 的收尾顺序）。
 * @param tryBlock 业务主体；[VmLoadingStyle.PAGE] 时请在此块末尾根据结果切换整页状态。
 */
fun BaseViewModel.launch(
    loadingStyle: VmLoadingStyle = VmLoadingStyle.NONE,
    catchBlock: suspend CoroutineScope.(Throwable) -> Unit = {},
    finallyBlock: suspend CoroutineScope.() -> Unit = {},
    tryBlock: suspend CoroutineScope.() -> Unit,
): Job =
    viewModelScope.launch {
        try {
            when (loadingStyle) {
                VmLoadingStyle.NONE -> Unit
                VmLoadingStyle.INLINE -> setLoadingInternal(true)
                VmLoadingStyle.PAGE -> showPageLoadingInternal()
            }
            tryBlock()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            catchBlock(e)
        } finally {
            finallyBlock()
            if (loadingStyle == VmLoadingStyle.INLINE) {
                setLoadingInternal(false)
            }
        }
    }

/**
 * 等价于 `launch(loadingStyle = INLINE, …)`。
 * 典型场景：列表刷新、表单提交，**行内** loading + 错误时在 [catchBlock] 里 [postError]。
 */
fun BaseViewModel.launchInlineLoading(
    catchBlock: suspend CoroutineScope.(Throwable) -> Unit = {},
    finallyBlock: suspend CoroutineScope.() -> Unit = {},
    tryBlock: suspend CoroutineScope.() -> Unit,
): Job = launch(
    loadingStyle = VmLoadingStyle.INLINE,
    catchBlock = catchBlock,
    finallyBlock = finallyBlock,
    tryBlock = tryBlock,
)

/**
 * 等价于 `launch(loadingStyle = PAGE, …)`。
 * 典型场景：进入页后首拉数据——`tryBlock` 里请求完成后再 [showPageEmpty]/[showPageError]/[showPageContent]。
 */
fun BaseViewModel.launchPageLoading(
    catchBlock: suspend CoroutineScope.(Throwable) -> Unit = {},
    finallyBlock: suspend CoroutineScope.() -> Unit = {},
    tryBlock: suspend CoroutineScope.() -> Unit,
): Job = launch(
    loadingStyle = VmLoadingStyle.PAGE,
    catchBlock = catchBlock,
    finallyBlock = finallyBlock,
    tryBlock = tryBlock,
)
