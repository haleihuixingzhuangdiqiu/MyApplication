package com.example.myapplication.mvvm

/**
 * 经 [BaseViewModel.userMessage] 传到 UI 的轻提示，决定 Toast 样式（与 [com.example.myapplication.common.toast] 扩展对应）。
 */
sealed class UiUserMessage {
    /**
     * 网络/系统类等失败，使用带叉的 error 样式（[com.example.myapplication.common.toast.showError]）。
     * 与链式里 [com.example.myapplication.mvvm.request.ApiRequestChain] 网络默认分支、固定网错等一致。
     */
    data class ErrorMessage(val errorMessage: String) : UiUserMessage()

    /**
     * 业务侧提示、一般轻提示，使用 info/纯文案样式（[com.example.myapplication.common.toast.showInfo]）。
     * 有壳 [ApiResponse] 封装码自动弹 message 等走此路。
     */
    data class InfoMessage(val text: String) : UiUserMessage()
}
