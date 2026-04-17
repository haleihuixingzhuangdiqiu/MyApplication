package com.example.myapplication.framework

/** 页面级一次性 UI 事件（通过 Flow 投递，在 Activity 中收集）。 */
sealed interface UiEvent {
    data class Toast(val message: String) : UiEvent
}
