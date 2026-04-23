package com.example.myapplication.mvvm

/**
 * **整页级**蒙层状态机（转圈、空、错、不挡内容），与 [com.example.myapplication.framework.PageOverlayHost] 的 UI 一一对应。
 *
 * ## 和 [com.example.myapplication.mvvm.BaseViewModel.loading] 的区别
 * - [com.example.myapplication.mvvm.BaseViewModel.loading]：行内/局部小菊花，**不盖满**屏幕，适合 SwipeRefresh、按钮旁。
 * - **本状态**：盖在**整页/整 Fragment 业务内容之上**，用于首屏、列表整体为空、整体请求失败等。
 *
 * ## 在 ViewModel 里怎么切状态
 * - 显示转圈 → [com.example.myapplication.mvvm.BaseViewModel.showPageLoading]（会设为 [Loading]）。
 * - 不挡内容、露出业务布局 → [com.example.myapplication.mvvm.BaseViewModel.showPageContent]（会设为 [Hidden]）。
 * - 无数据 / 错误 → [com.example.myapplication.mvvm.BaseViewModel.showPageEmpty] / [com.example.myapplication.mvvm.BaseViewModel.showPageError]。
 *
 * 数据流为 [com.example.myapplication.mvvm.BaseViewModel.pageOverlay]；Activity/Fragment 侧由 `PageOverlayHost.render` 或自写 `when` 分支。
 */
sealed interface PageOverlayState {

    /**
     * 不展示蒙层，**业务内容可交互**。
     * 与 [com.example.myapplication.mvvm.BaseViewModel.showPageContent] 对应（语义：展示页面内容，而非「隐藏某个 View id」）。
     */
    data object Hidden : PageOverlayState

    /** 整页加载中（通常为大号 Progress / 全屏转圈，由 [PageOverlayHost] 决定具体样式）。 */
    data object Loading : PageOverlayState

    /**
     * 有结构但无数据；[hint] 为 `null` 时由布局内默认空态字符串兜底。
     */
    data class Empty(val hint: String? = null) : PageOverlayState

    /**
     * 整页错误说明 + 是否提供重试。
     * @param allowRetry 为 `true` 时显示重试按钮；点击后走页面 `onPageOverlayRetry` 或 [com.example.myapplication.mvvm.BaseViewModel.onPageOverlayRetry]（见 :core:framework 的 [com.example.myapplication.framework.BaseBindingActivity]）。
     */
    data class Error(val message: String, val allowRetry: Boolean = true) : PageOverlayState
}
