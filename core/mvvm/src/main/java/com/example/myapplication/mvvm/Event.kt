package com.example.myapplication.mvvm

/**
 * **一次性**事件封装，避免 `SharedFlow` / 多次 `collect` 时同一条提示被展示多遍（例如配置变更后重组、从栈底返回）。
 *
 * ## 使用方式
 * - 在 UI 层（或 `bindBaseViewModelUi`）对 [com.example.myapplication.mvvm.BaseViewModel.userMessage] 做 `collect` 时，
 *   先调 [getContentIfNotHandled]：第一次返回载荷，之后同对象再调返回 `null`。
 * - [peek] 为调试或「只读不消费」设计，生产路径慎用，否则可能绕开「只弹一次」的语义。
 *
 * @param T 在 [com.example.myapplication.mvvm.BaseViewModel.userMessage] 上为 [com.example.myapplication.mvvm.UiUserMessage]；也用于其他一次性载荷。
 */
class Event<out T>(private val content: T) {
    private var consumed = false

    /**
     * 若本事件**尚未**被消费，则标记为已消费并返回 [content]；否则返回 `null`。
     * **每次 collect 到同一个 [Event] 实例时，仅第一次会拿到非空值。**
     */
    fun getContentIfNotHandled(): T? {
        if (consumed) return null
        consumed = true
        return content
    }

    /**
     * 总是返回 [content]，**不改变**已消费状态。适合日志或需多次读取的调试；若用于直接弹 Toast 会破坏一次性语义。
     */
    fun peek(): T = content
}
