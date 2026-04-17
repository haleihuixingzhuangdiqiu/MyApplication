package com.example.myapplication.framework

/**
 * 页面级全屏遮罩：加载中 / 空数据 / 错误（可选重试）。
 *
 * 由 [BaseViewModel] 的 [BaseViewModel.pageOverlay] 驱动，
 * [BaseBindingActivity] / [BaseBindingFragment] 在 [enablePageOverlay] 为 true 时自动渲染。
 */
sealed interface PageOverlayState {

    /** 不展示遮罩，内容可交互。 */
    data object Hidden : PageOverlayState

    data object Loading : PageOverlayState

    /** [hint] 为空时使用布局内默认「暂无数据」文案。 */
    data class Empty(val hint: String? = null) : PageOverlayState

    /**
     * @param message 错误说明
     * @param allowRetry 为 false 时隐藏重试按钮；为 true 时点击走 ViewModel 的 [BaseViewModel.onPageOverlayRetry] 或页面的 onPageOverlayRetry 回调
     */
    data class Error(val message: String, val allowRetry: Boolean = true) : PageOverlayState
}
