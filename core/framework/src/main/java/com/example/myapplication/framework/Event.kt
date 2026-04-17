package com.example.myapplication.framework

/**
 * 单次消费的封装（用于导航 / Toast 等一次性事件，避免重复触发）。
 */
class Event<out T>(private val content: T) {
    private var consumed = false

    fun getContentIfNotHandled(): T? {
        if (consumed) return null
        consumed = true
        return content
    }

    fun peek(): T = content
}
